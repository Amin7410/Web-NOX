package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.SocialIdentity;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
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

    @Value("${security.login.max-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.login.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${security.jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    @Transactional
    public AuthResult authenticate(String email, String plaintextPassword, String ipAddress, String userAgent) {
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
            user.getSecurity().resetFailedLogins();
        } catch (Exception e) {
            user.getSecurity().incrementFailedLogins();
            if (user.getSecurity().getFailedLoginAttempts() >= maxLoginAttempts) {
                user.getSecurity().lockAccount(lockoutDurationMinutes);
            }
            userRepository.save(user);
            throw new DomainException("INVALID_CREDENTIALS", "Invalid email or password", 401);
        }

        userRepository.save(user);

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
            throw new DomainException("EXP_REFRESH_TOKEN", "Refresh token has expired or been revoked", 401);
        }

        User user = session.getUser();
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
        String email = (String) verifiedData.get("email");
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

        if (user.getSecurity() != null && user.getSecurity().isMfaEnabled()) {
            String mfaToken = jwtService.generateToken(java.util.Map.of("mfa_pending", true), user.getEmail());
            return new AuthResult(null, null, null, true, mfaToken);
        }

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public record AuthResult(User user, String token, String refreshToken, boolean mfaRequired, String mfaToken) {
    }
}
