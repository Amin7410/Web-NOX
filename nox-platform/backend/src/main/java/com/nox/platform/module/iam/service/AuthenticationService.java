package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.iam.service.internal.InternalSecurityStateService;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("[AUTH] Attempting login for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[AUTH] User not found with email: {}", email);
                    return new DomainException("INVALID_CREDENTIALS", "Invalid email or password", 401);
                });

        log.info("[AUTH] User found: {} with status: {}", user.getEmail(), user.getStatus());

        if (user.getSecurity().isLocked()) {
            log.warn("[AUTH] Account locked for user: {}", email);
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked due to too many failed attempts",
                    423);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("[AUTH] Account not active (status: {}) for user: {}", user.getStatus(), email);
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Please verify your email", 403);
        }

        try {
            log.debug("[AUTH] Authenticating with AuthenticationManager for user: {}", email);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, plaintextPassword));
            log.info("[AUTH] Successful authentication for user: {}", email);
            internalSecurityStateService.resetFailedLogins(user.getId());
        } catch (Exception e) {
            log.error("[AUTH] Authentication failed for user: {}. Error: {}", email, e.getMessage());
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
