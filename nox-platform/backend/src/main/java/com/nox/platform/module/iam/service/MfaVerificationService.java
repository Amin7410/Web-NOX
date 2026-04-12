package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.service.abstraction.TokenProvider;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MfaVerificationService {

    private final UserRepository userRepository;
    private final UserMfaBackupCodeRepository userMfaBackupCodeRepository;
    private final MfaService mfaService;
    private final UserSecurityRepository userSecurityRepository;
    private final TokenProvider tokenProvider;
    private final TimeProvider timeProvider;
    private final UserSessionService userSessionService;

    public AuthenticationService.AuthResult verifyMfa(String mfaToken, int code, String ipAddress, String userAgent) {
        User user = validateMfaTokenAndGetUser(mfaToken);

        if (!user.getSecurity().isMfaEnabled() || user.getSecurity().getMfaSecret() == null) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user");
        }

        if (!mfaService.verifyCode(user.getSecurity().getMfaSecret(), code)) {
            user.getSecurity().incrementFailedMfaAttempts(timeProvider.now());
            if (user.getSecurity().getFailedMfaAttempts() >= 5) {
                user.getSecurity().lockAccount(timeProvider.now(), 15);
                userSecurityRepository.save(user.getSecurity());
                throw new DomainException("ACCOUNT_LOCKED", "Too many failed attempts. Account locked for 15 minutes.");
            }
            userSecurityRepository.save(user.getSecurity());
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Please try again.");
        }

        user.getSecurity().resetFailedLogins();
        userSecurityRepository.save(user.getSecurity());

        return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    public AuthenticationService.AuthResult verifyMfaBackupCode(String mfaToken, String backupCode, String ipAddress,
            String userAgent) {
        User user = validateMfaTokenAndGetUser(mfaToken);

        if (!user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user");
        }

        if (user.getSecurity().isLocked(timeProvider.now())) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked due to security attempts");
        }

        List<UserMfaBackupCode> backupCodes = userMfaBackupCodeRepository.findByUserAndUsedFalse(user);

        UserMfaBackupCode matchedCode = null;
        String hashedInputCode = DigestUtils.sha256Hex(backupCode);
        for (UserMfaBackupCode storedCode : backupCodes) {
            if (hashedInputCode.equals(storedCode.getCodeHash())) {
                matchedCode = storedCode;
                break;
            }
        }

        if (matchedCode == null) {
            user.getSecurity().incrementFailedMfaAttempts(timeProvider.now());
            if (user.getSecurity().getFailedMfaAttempts() >= 5) {
                user.getSecurity().lockAccount(timeProvider.now(), 15);
                userSecurityRepository.save(user.getSecurity());
                throw new DomainException("ACCOUNT_LOCKED", "Too many failed attempts. Account locked for 15 minutes.");
            }
            userSecurityRepository.save(user.getSecurity());
            throw new DomainException("INVALID_BACKUP_CODE", "Invalid or already used backup code.");
        }

        user.getSecurity().resetFailedLogins();
        userSecurityRepository.save(user.getSecurity());

        matchedCode.markAsUsed(timeProvider.now());
        userMfaBackupCodeRepository.save(matchedCode);

        return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    private User validateMfaTokenAndGetUser(String mfaToken) {
        String userEmail = tokenProvider.extractUsername(mfaToken);
        Boolean isMfaPending = tokenProvider.extractClaim(mfaToken, claims -> claims.get("mfa_pending", Boolean.class));

        if (isMfaPending == null || !isMfaPending) {
            throw new DomainException("INVALID_MFA_TOKEN", "Provided token is not a valid MFA pending token");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active");
        }

        if (user.getSecurity().isLocked(timeProvider.now())) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked");
        }

        return user;
    }
}


