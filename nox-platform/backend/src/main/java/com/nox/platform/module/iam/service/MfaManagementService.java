package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Service dedicated to managing User MFA settings architectures (Setup, Enable,
 * Disable).
 */
@Service
@RequiredArgsConstructor
public class MfaManagementService {

    private final UserRepository userRepository;
    private final UserMfaBackupCodeRepository userMfaBackupCodeRepository;
    private final MfaService mfaService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Value("${security.mfa.backup-codes.count:10}")
    private int backupCodesCount;

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
    public void disableMfa(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (!user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user", 400);
        }

        if (user.getSecurity().isLocked()) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked", 423);
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
            if (!passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
                throw new DomainException("INVALID_CREDENTIALS", "Invalid password", 401);
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            throw new DomainException("USER_NOT_FOUND", "User not found", 404);
        }

        user.getSecurity().setMfaEnabled(false);
        user.getSecurity().setMfaSecret(null);
        user.getSecurity().setTempMfaSecret(null);
        userRepository.save(user);

        userMfaBackupCodeRepository.deleteByUser(user);
    }

    public record MfaSetupResult(String secret, String qrCodeUri) {
    }
}
