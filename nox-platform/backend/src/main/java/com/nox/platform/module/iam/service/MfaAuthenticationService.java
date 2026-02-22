package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MfaAuthenticationService {

    private final UserRepository userRepository;
    private final UserMfaBackupCodeRepository userMfaBackupCodeRepository;
    private final MfaService mfaService;
    private final UserSecurityRepository userSecurityRepository;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @Value("${security.mfa.backup-codes.count:10}")
    private int backupCodesCount;

    @Transactional
    public AuthenticationService.AuthResult verifyMfa(String mfaToken, int code, String ipAddress, String userAgent) {
        User user = validateMfaTokenAndGetUser(mfaToken);

        if (!user.getSecurity().isMfaEnabled() || user.getSecurity().getMfaSecret() == null) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user", 400);
        }

        if (!mfaService.verifyCode(user.getSecurity().getMfaSecret(), code)) {
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Please try again.", 401);
        }

        return authenticationService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    @Transactional
    public MfaSetupResult setupMfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_ALREADY_ENABLED", "MFA is already enabled for this user", 400);
        }

        String secret = mfaService.generateSecretKey();
        user.getSecurity().setTempMfaSecret(secret);
        userRepository.save(user);

        String qrCodeUri = mfaService.getQrCodeUri(secret, user.getEmail());
        return new MfaSetupResult(secret, qrCodeUri);
    }

    @Transactional
    public List<String> enableMfa(String email, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        String tempSecret = user.getSecurity().getTempMfaSecret();
        if (tempSecret == null) {
            throw new DomainException("MFA_SETUP_REQUIRED", "MFA setup was not initiated", 400);
        }

        if (!mfaService.verifyCode(tempSecret, code)) {
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Verification failed.", 400);
        }

        user.getSecurity().setMfaEnabled(true);
        user.getSecurity().setMfaSecret(tempSecret);
        user.getSecurity().setTempMfaSecret(null);
        userRepository.save(user);

        userMfaBackupCodeRepository.deleteByUser(user);

        List<String> plainBackupCodes = new ArrayList<>();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < backupCodesCount; i++) {
            byte[] randomBytes = new byte[5];
            secureRandom.nextBytes(randomBytes);
            String plainCode = org.apache.commons.codec.binary.Hex.encodeHexString(randomBytes).toUpperCase()
                    .substring(0, 8);
            plainBackupCodes.add(plainCode);

            UserMfaBackupCode backupCode = UserMfaBackupCode.builder()
                    .user(user)
                    .codeHash(DigestUtils.sha256Hex(plainCode))
                    .used(false)
                    .build();
            userMfaBackupCodeRepository.save(backupCode);
        }

        return plainBackupCodes;
    }

    @Transactional
    public AuthenticationService.AuthResult verifyMfaBackupCode(String mfaToken, String backupCode, String ipAddress,
            String userAgent) {
        User user = validateMfaTokenAndGetUser(mfaToken);

        if (!user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user", 400);
        }

        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked due to security attempts", 423);
        }

        List<UserMfaBackupCode> backupCodes = userMfaBackupCodeRepository.findByUserAndUsedFalse(user);

        String hashedInput = DigestUtils.sha256Hex(backupCode);
        UserMfaBackupCode matchedCode = null;
        for (UserMfaBackupCode storedCode : backupCodes) {
            if (hashedInput.equals(storedCode.getCodeHash())) {
                matchedCode = storedCode;
                break;
            }
        }

        if (matchedCode == null) {
            user.getSecurity().incrementFailedMfaAttempts();
            if (user.getSecurity().getFailedMfaAttempts() >= 5) {
                user.getSecurity().lockAccount(15);
                userSecurityRepository.save(user.getSecurity());
                throw new DomainException("ACCOUNT_LOCKED", "Too many failed attempts. Account locked for 15 minutes.",
                        423);
            }
            userSecurityRepository.save(user.getSecurity());
            throw new DomainException("INVALID_BACKUP_CODE", "Invalid or already used backup code.", 401);
        }

        user.getSecurity().resetFailedLogins();
        userSecurityRepository.save(user.getSecurity());

        matchedCode.setUsed(true);
        userMfaBackupCodeRepository.save(matchedCode);

        return authenticationService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    private User validateMfaTokenAndGetUser(String mfaToken) {
        String email = jwtService.extractUsername(mfaToken);
        Boolean isMfaPending = jwtService.extractClaim(mfaToken, claims -> claims.get("mfa_pending", Boolean.class));

        if (isMfaPending == null || !isMfaPending) {
            throw new DomainException("INVALID_MFA_TOKEN", "Provided token is not a valid MFA pending token", 401);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (user.getStatus() != com.nox.platform.module.iam.domain.UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active", 403);
        }

        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked", 423);
        }

        return user;
    }

    public record MfaSetupResult(String secret, String qrCodeUri) {
    }
}
