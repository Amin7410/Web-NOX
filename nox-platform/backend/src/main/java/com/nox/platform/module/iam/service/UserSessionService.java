package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.DeviceUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Service responsible for managing the lifecycle of User Sessions and Tokens
 * across the system.
 */
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;

    @Value("${security.jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    public AuthenticationService.AuthResult generateSuccessAuthResult(User user, String ipAddress,
            String rawUserAgent) {
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

        return new AuthenticationService.AuthResult(user, jwtToken, refreshToken, false, null);
    }

    @Transactional
    public AuthenticationService.AuthResult refreshAccessToken(String refreshToken, String ipAddress,
            String userAgent) {
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
                    throw new DomainException("INVALID_REFRESH_TOKEN",
                            "Token recently rotated. Please use the new token or login again.", 401);
                }
            }

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

        return new AuthenticationService.AuthResult(user, newJwtToken, newRefreshToken, false, null);
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

}
