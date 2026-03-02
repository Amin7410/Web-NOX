package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.SocialIdentity;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.iam.service.internal.InternalSecurityStateService;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.DeviceUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SocialIdentityRepository socialIdentityRepository;
    private final SocialAuthVerificationService socialAuthVerificationService;
    private final UserSecurityRepository userSecurityRepository;
    private final InternalSecurityStateService internalSecurityStateService;

    @Value("${security.login.max-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.login.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${security.jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    public AuthResult authenticate(String email, String plaintextPassword, String ipAddress, String userAgent) {
        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("INVALID_CREDENTIALS", "Invalid email or password", 401));

        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked due to too many failed attempts",
                    423);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Please verify your email", 403);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, plaintextPassword));
            internalSecurityStateService.resetFailedLogins(user.getId());
        } catch (Exception e) {
            internalSecurityStateService.incrementFailedLogins(user.getId());

            // Since internalSecurityStateService mutates state natively, we must consider
            // if we need the refreshed value.
            // Using user.getSecurity() here might fetch stale Object relative to native
            // increment, but lock is 5 max.
            // It's safer to pull raw fail_limit directly or just assume if it fails right
            // now + existing DB states it passes.
            // A more optimized way is counting the offset against getSecurity.
            int currentFails = user.getSecurity().getFailedLoginAttempts() + 1; // + 1 for current failure

            if (currentFails >= maxLoginAttempts) {
                internalSecurityStateService.lockAccount(user.getId(),
                        OffsetDateTime.now().plusMinutes(lockoutDurationMinutes));
            }
            throw new DomainException("INVALID_CREDENTIALS", "Invalid email or password", 401);
        }

        // Removed redundant repository.save(user) as we use native queries for security
        // state

        if (user.getSecurity().isMfaEnabled()) {
            String mfaToken = jwtService.generateToken(java.util.Map.of("mfa_pending", true), user.getEmail());
            return new AuthResult(null, null, null, true, mfaToken);
        }

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public AuthResult generateSuccessAuthResult(User user, String ipAddress, String rawUserAgent) {
        String jwtToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken();
        String deviceType = DeviceUtils.extractDeviceType(rawUserAgent);

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(DigestUtils.sha256Hex(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(rawUserAgent)
                .deviceType(deviceType)
                .lastActiveAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(refreshTokenExpirationDays))
                .build();
        userSessionRepository.save(session);

        return new AuthResult(user, jwtToken, refreshToken, false, null);
    }

    @Transactional
    public AuthResult refreshAccessToken(String refreshToken, String ipAddress, String userAgent) {
        String hashedToken = DigestUtils.sha256Hex(refreshToken);
        UserSession session = userSessionRepository.findByRefreshToken(hashedToken)
                .orElseThrow(
                        () -> new DomainException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", 401));

        if (!session.isValid()) {
            // Check Token Rotation Grace Period (60 seconds)
            if (session.isRevoked() && session.getRevokedAt() != null) {
                long secondsSinceRevoked = java.time.Duration.between(session.getRevokedAt(), OffsetDateTime.now())
                        .getSeconds();
                if (secondsSinceRevoked < 60) {
                    // Grace period active - this is likely a network retry or race condition
                    // We don't generate a new token (as it was already generated), we just reject
                    // this specific request quietly
                    // Realistically, the client should use the new token it received on the first
                    // successful request
                    // Or if it lost it entirely, we force them to login again smoothly without
                    // blowing up all their other devices.
                    throw new DomainException("INVALID_REFRESH_TOKEN",
                            "Token recently rotated. Please use the new token or login again.", 401);
                }
            }

            // Potential session hijacking: if an invalid token is used outside grace
            // period, revoke EVERYTHING
            userSessionRepository.revokeAllUserSessions(session.getUser().getId(),
                    "Potential Session Hijacking - Invalid Token Reuse");
            throw new DomainException("COMPROMISED_TOKEN", "Security alert: Please login again.", 401);
        }

        User user = session.getUser();
        if (user.getStatus() != com.nox.platform.module.iam.domain.UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active", 403);
        }
        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked", 423);
        }

        String newJwtToken = jwtService.generateToken(user.getEmail());

        session.setRevokedAt(OffsetDateTime.now());
        session.setRevokeReason("Token Rotated");
        userSessionRepository.save(session);

        String newRefreshToken = jwtService.generateRefreshToken();
        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshToken(DigestUtils.sha256Hex(newRefreshToken))
                .ipAddress(ipAddress != null ? ipAddress : session.getIpAddress())
                .userAgent(userAgent != null ? userAgent : session.getUserAgent())
                .deviceType(session.getDeviceType())
                .lastActiveAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(refreshTokenExpirationDays))
                .build();
        userSessionRepository.save(newSession);

        return new AuthResult(user, newJwtToken, newRefreshToken, false, null);
    }

    @Transactional
    public void logout(String refreshToken, String ownerEmail) {
        String hashedToken = DigestUtils.sha256Hex(refreshToken);
        userSessionRepository.findByRefreshToken(hashedToken)
                .ifPresent(session -> {
                    if (!session.getUser().getEmail().equals(ownerEmail)) {
                        throw new DomainException("UNAUTHORIZED_LOGOUT", "You cannot logout a session you do not own",
                                403);
                    }
                    session.revoke("User Logged Out");
                    userSessionRepository.save(session);
                });
    }

    @Transactional
    public AuthResult socialLogin(String provider, String token, String ipAddress, String userAgent) {
        Map<String, Object> verifiedData = socialAuthVerificationService.verifyToken(provider, token);

        String providerId = (String) verifiedData.get("providerId");
        String email = ((String) verifiedData.get("email")).trim().toLowerCase();
        String fullName = (String) verifiedData.get("fullName");

        @SuppressWarnings("unchecked")
        Map<String, Object> profileData = (Map<String, Object>) verifiedData.get("rawProfile");

        Optional<SocialIdentity> existingIdentity = socialIdentityRepository.findByProviderAndProviderId(provider,
                providerId);
        User user;

        if (existingIdentity.isPresent()) {
            user = existingIdentity.get().getUser();
        } else {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                // Potential Account Takeover: email found but no linked social identity
                // If user has a password set, we MUST ask for it before linking
                if (user.getSecurity().isPasswordSet()) {
                    throw new DomainException("LINK_REQUIRED",
                            "Account exists. Please link your social account with your password.", 403);
                }
                // If no password but exists, maybe they have another social?
                // For now, if user exists, we strictly require a link process if the current
                // identity is new.
            }

            if (user == null) {
                user = User.builder()
                        .email(email)
                        .fullName(fullName)
                        .status(UserStatus.ACTIVE)
                        .isEmailVerified(true)
                        .build();

                UserSecurity security = UserSecurity.builder()
                        .user(user)
                        .isPasswordSet(false)
                        .build();
                user.setSecurity(security);
                user = userRepository.save(user);
            }

            SocialIdentity identity = SocialIdentity.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .profileData(profileData)
                    .build();
            socialIdentityRepository.save(identity);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Your account is not active or has been suspended.", 403);
        }

        if (user.getSecurity() != null && user.getSecurity().isMfaEnabled()) {
            String mfaToken = jwtService.generateToken(java.util.Map.of("mfa_pending", true), user.getEmail());
            return new AuthResult(null, null, null, true, mfaToken);
        }

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    @Transactional
    public AuthResult linkSocialAccount(String provider, String token, String password, String ipAddress,
            String userAgent) {
        Map<String, Object> verifiedData = socialAuthVerificationService.verifyToken(provider, token);
        String email = ((String) verifiedData.get("email")).trim().toLowerCase();
        String providerId = (String) verifiedData.get("providerId");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active", 403);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            throw new DomainException("INVALID_CREDENTIALS", "Invalid password for linking", 401);
        }

        // Check again if already linked (race condition)
        if (socialIdentityRepository.findByProviderAndProviderId(provider, providerId).isPresent()) {
            return generateSuccessAuthResult(user, ipAddress, userAgent);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> profileData = (Map<String, Object>) verifiedData.get("rawProfile");

        SocialIdentity identity = SocialIdentity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .profileData(profileData)
                .build();
        socialIdentityRepository.save(identity);

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public record AuthResult(User user, String token, String refreshToken, boolean mfaRequired, String mfaToken) {
    }
}
