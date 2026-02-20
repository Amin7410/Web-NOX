package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

        return userRepository.save(user);
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

        return new AuthResult(user, jwtToken, refreshToken);
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
        return new AuthResult(user, newJwtToken, session.getRefreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.revoke("User Logged Out");
                    userSessionRepository.save(session);
                });
    }

    public record AuthResult(User user, String token, String refreshToken) {
    }
}
