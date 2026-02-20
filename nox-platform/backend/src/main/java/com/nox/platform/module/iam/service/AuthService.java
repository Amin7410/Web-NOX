package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.SocialIdentity;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final MfaService mfaService;
    private final SocialIdentityRepository socialIdentityRepository;

    @Transactional
    public User registerUser(String email, String plaintextPassword, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("EMAIL_ALREADY_EXISTS", "A user with this email already exists", 400);
        }

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        String hashedPassword = passwordEncoder.encode(plaintextPassword);

        UserSecurity security = UserSecurity.builder()
                .user(user)
                .passwordHash(hashedPassword)
                .isPasswordSet(true)
                .build();

        user.setSecurity(security);

        user = userRepository.save(user);

        // Generate and send verification email
        OtpCode otp = otpService.generateOtp(user, OtpCode.OtpType.VERIFY_EMAIL);
        emailService.sendVerificationEmail(user.getEmail(), otp.getCode());

        return user;
    }

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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, plaintextPassword));

            // Success Path
            user.getSecurity().resetFailedLogins();
        } catch (Exception e) {
            // Failure Path
            user.getSecurity().incrementFailedLogins();
            if (user.getSecurity().getFailedLoginAttempts() >= 5) {
                user.getSecurity().lockAccount(15); // Lock for 15 minutes
            }
            userRepository.save(user);
            throw new DomainException("INVALID_CREDENTIALS", "Invalid email or password", 401);
        }

        userRepository.save(user);

        if (user.getSecurity().isMfaEnabled()) {
            // Generate a short-lived token specifically for MFA step
            String mfaToken = jwtService.generateToken(java.util.Map.of("mfa_pending", true), user.getEmail());
            return new AuthResult(null, null, null, true, mfaToken);
        }

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    private AuthResult generateSuccessAuthResult(User user, String ipAddress, String userAgent) {
        String jwtToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken();

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastActiveAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(7)) // Refresh token valid for 7 days
                .build();
        userSessionRepository.save(session);

        return new AuthResult(user, jwtToken, refreshToken, false, null);
    }

    @Transactional
    public AuthResult refreshAccessToken(String refreshToken, String ipAddress, String userAgent) {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(
                        () -> new DomainException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", 401));

        if (!session.isValid()) {
            throw new DomainException("EXP_REFRESH_TOKEN", "Refresh token has expired or been revoked", 401);
        }

        // Update last active, IP and User Agent
        session.setLastActiveAt(OffsetDateTime.now());
        if (ipAddress != null)
            session.setIpAddress(ipAddress);
        if (userAgent != null)
            session.setUserAgent(userAgent);

        userSessionRepository.save(session);

        User user = session.getUser();
        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked", 423);
        }

        String newJwtToken = jwtService.generateToken(user.getEmail());

        // We do not rotate refresh token here, but we could if strict rotation is
        // required
        return new AuthResult(user, newJwtToken, session.getRefreshToken(), false, null);
    }

    @Transactional
    public void logout(String refreshToken) {
        userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.revoke("User Logged Out");
                    userSessionRepository.save(session);
                });
    }

    @Transactional
    public void verifyEmail(String otpCode) {
        OtpCode otp = otpService.validateAndUseOtp(otpCode, OtpCode.OtpType.VERIFY_EMAIL);
        User user = otp.getUser();

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new DomainException("USER_ALREADY_ACTIVE", "This account is already verified.", 400);
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) {
                return; // Do not send reset emails to unverified accounts
            }
            OtpCode otp = otpService.generateOtp(user, OtpCode.OtpType.RESET_PASSWORD);
            emailService.sendPasswordResetEmail(user.getEmail(), otp.getCode());
        });
    }

    @Transactional
    public void resetPassword(String otpCode, String newPassword) {
        OtpCode otp = otpService.validateAndUseOtp(otpCode, OtpCode.OtpType.RESET_PASSWORD);
        User user = otp.getUser();

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.getSecurity().setPasswordHash(hashedPassword);
        user.getSecurity().setPasswordSet(true);
        userRepository.save(user);
    }

    @Transactional
    public AuthResult verifyMfa(String mfaToken, int code, String ipAddress, String userAgent) {
        String email = jwtService.extractUsername(mfaToken);
        Boolean isMfaPending = jwtService.extractClaim(mfaToken, claims -> claims.get("mfa_pending", Boolean.class));

        if (isMfaPending == null || !isMfaPending) {
            throw new DomainException("INVALID_MFA_TOKEN", "Provided token is not a valid MFA pending token", 401);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (!user.getSecurity().isMfaEnabled() || user.getSecurity().getMfaSecret() == null) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user", 400);
        }

        if (!mfaService.verifyCode(user.getSecurity().getMfaSecret(), code)) {
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Please try again.", 401);
        }

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    @Transactional
    public MfaSetupResult setupMfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_ALREADY_ENABLED", "MFA is already enabled for this user", 400);
        }

        String secret = mfaService.generateSecretKey();
        String qrCodeUri = mfaService.getQrCodeUri(secret, user.getEmail());
        return new MfaSetupResult(secret, qrCodeUri);
    }

    @Transactional
    public void enableMfa(String email, String secret, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (!mfaService.verifyCode(secret, code)) {
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Verification failed.", 400);
        }

        user.getSecurity().setMfaEnabled(true);
        user.getSecurity().setMfaSecret(secret);
        userRepository.save(user);
    }

    @Transactional
    public AuthResult socialLogin(String provider, String providerId, String email, String fullName,
            Map<String, Object> profileData, String ipAddress, String userAgent) {
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

        return generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public record AuthResult(User user, String token, String refreshToken, boolean mfaRequired, String mfaToken) {
    }

    public record MfaSetupResult(String secret, String qrCodeUri) {
    }
}
