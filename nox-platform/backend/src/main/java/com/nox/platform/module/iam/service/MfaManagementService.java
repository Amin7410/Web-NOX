package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.abstraction.TimeProvider;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MfaManagementService {

    private final UserRepository userRepository;
    private final UserMfaBackupCodeRepository userMfaBackupCodeRepository;
    private final MfaService mfaService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final TimeProvider timeProvider;

    @Value("${security.mfa.backup-codes.count:10}")
    private int backupCodesCount;

    @Transactional
    public MfaSetupResult setupMfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        if (user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_ALREADY_ENABLED", "MFA is already enabled for this user");
        }

        String secret = mfaService.generateSecretKey();
        user.getSecurity().initMfa(secret);
        userRepository.save(user);

        String qrCodeUri = mfaService.getQrCodeUri(secret, user.getEmail());
        return new MfaSetupResult(secret, qrCodeUri);
    }

    @Transactional
    public List<String> enableMfa(String email, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        String tempSecret = user.getSecurity().getTempMfaSecret();
        if (tempSecret == null) {
            throw new DomainException("MFA_SETUP_REQUIRED", "MFA setup was not initiated");
        }

        if (!mfaService.verifyCode(tempSecret, code)) {
            throw new DomainException("INVALID_MFA_CODE", "Invalid MFA code. Verification failed.");
        }

        user.getSecurity().activateMfa(tempSecret);
        userRepository.save(user);

        userMfaBackupCodeRepository.deleteByUser(user);

        OffsetDateTime now = timeProvider.now();
        List<String> plainBackupCodes = new ArrayList<>();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < backupCodesCount; i++) {
            byte[] randomBytes = new byte[5];
            secureRandom.nextBytes(randomBytes);
            String plainCode = org.apache.commons.codec.binary.Hex.encodeHexString(randomBytes).toUpperCase()
                    .substring(0);
            plainBackupCodes.add(plainCode);

            UserMfaBackupCode backupCode = UserMfaBackupCode.builder()
                    .user(user)
                    .codeHash(DigestUtils.sha256Hex(plainCode))
                    .used(false)
                    .build();
            backupCode.initializeTimestamps(now);
            userMfaBackupCodeRepository.save(backupCode);
        }

        return plainBackupCodes;
    }

    @Transactional
    public void disableMfa(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        if (!user.getSecurity().isMfaEnabled()) {
            throw new DomainException("MFA_NOT_ENABLED", "MFA is not enabled for this user");
        }

        if (user.getSecurity().isLocked(timeProvider.now())) {
            throw new DomainException("ACCOUNT_LOCKED", "Account is temporarily locked");
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
            if (!passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
                throw new DomainException("INVALID_CREDENTIALS", "Invalid password");
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            throw new DomainException("USER_NOT_FOUND", "User not found");
        }

        user.getSecurity().disableMfa();
        userRepository.save(user);

        userMfaBackupCodeRepository.deleteByUser(user);
    }

    public record MfaSetupResult(String secret, String qrCodeUri) {
    }
}


