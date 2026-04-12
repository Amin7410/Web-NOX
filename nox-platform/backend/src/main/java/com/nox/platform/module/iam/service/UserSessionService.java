package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.service.abstraction.TokenProvider;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.DeviceUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final TokenProvider tokenProvider;
    private final TimeProvider timeProvider;

    @Value("${security.jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    public AuthenticationService.AuthResult generateSuccessAuthResult(User user, String ipAddress,
            String rawUserAgent) {
        String accessToken = tokenProvider.generateToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken();
        String deviceType = DeviceUtils.extractDeviceType(rawUserAgent);

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(DigestUtils.sha256Hex(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(rawUserAgent)
                .deviceType(deviceType)
                .lastActiveAt(timeProvider.now())
                .expiresAt(timeProvider.now().plusDays(refreshTokenExpirationDays))
                .build();
        userSessionRepository.save(session);

        return new AuthenticationService.AuthResult(user, accessToken, refreshToken, false, null);
    }

    @Transactional
    public AuthenticationService.AuthResult refreshAccessToken(String refreshToken, String ipAddress,
            String userAgent) {
        String hashedToken = DigestUtils.sha256Hex(refreshToken);
        UserSession session = userSessionRepository.findByRefreshToken(hashedToken)
                .orElseThrow(
                        () -> new DomainException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", 401));

        if (!session.isValid(timeProvider.now())) {
            // Check Token Rotation Grace Period (60 seconds)
            if (session.isRevoked() && session.getRevokedAt() != null) {
                long secondsSinceRevoked = java.time.Duration.between(session.getRevokedAt(), timeProvider.now())
                        .getSeconds();
                if (secondsSinceRevoked < 60) {
                    throw new DomainException("INVALID_REFRESH_TOKEN",
                            "Token recently rotated. Please use the new token or login again.", 401);
                }
            }

            userSessionRepository.revokeAllUserSessions(session.getUser().getId(),
                    "Potential Session Hijacking - Invalid Token Reuse", timeProvider.now());
            throw new DomainException("COMPROMISED_TOKEN", "Security alert: Please login again.", 401);
        }

        User user = session.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active", 403);
        }
        if (user.getSecurity().isLocked(timeProvider.now())) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked", 423);
        }

        String newJwtToken = tokenProvider.generateToken(user.getEmail());

        session.setRevokedAt(timeProvider.now());
        session.setRevokeReason("Token Rotated");
        userSessionRepository.save(session);

        String newRefreshToken = tokenProvider.generateRefreshToken();
        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshToken(DigestUtils.sha256Hex(newRefreshToken))
                .ipAddress(ipAddress != null ? ipAddress : session.getIpAddress())
                .userAgent(userAgent != null ? userAgent : session.getUserAgent())
                .deviceType(session.getDeviceType())
                .lastActiveAt(timeProvider.now())
                .expiresAt(timeProvider.now().plusDays(refreshTokenExpirationDays))
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
                    session.revoke("User Logged Out", timeProvider.now());
                    userSessionRepository.save(session);
                });
    }

}
