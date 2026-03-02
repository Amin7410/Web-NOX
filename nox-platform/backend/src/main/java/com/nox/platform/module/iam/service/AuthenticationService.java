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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final InternalSecurityStateService internalSecurityStateService;
    private final UserSessionService userSessionService;

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

        return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public record AuthResult(User user, String token, String refreshToken, boolean mfaRequired, String mfaToken) {
    }
}
