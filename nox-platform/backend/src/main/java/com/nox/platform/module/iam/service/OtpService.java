package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.OtpCodeRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;

    @Value("${security.otp.length:6}")
    private int otpLength = 6;

    @Value("${security.otp.expiry-minutes:15}")
    private int otpExpiryMinutes = 15;

    @Value("${security.otp.max-attempts:5}")
    private int maxAttempts = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OtpCode generateOtp(User user, OtpCode.OtpType type) {
        // Check for cooldown (60 seconds)
        otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), type)
                .ifPresent(latestOtp -> {
                    if (latestOtp.getCreatedAt().isAfter(OffsetDateTime.now().minusSeconds(60))) {
                        throw new DomainException("PLEASE_WAIT",
                                "Please wait at least 60 seconds before requesting a new OTP.", 429);
                    }
                });

        // Invalidate any existing unused OTP of the same type for this user
        otpCodeRepository.invalidatePreviousOtps(user.getId(), type);

        String code = generateRandomCode();

        OtpCode otpCode = OtpCode.builder()
                .user(user)
                .code(code)
                .type(type)
                .expiresAt(OffsetDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        return otpCodeRepository.save(otpCode);
    }

    public OtpCode validateAndUseOtp(User user, String code, OtpCode.OtpType type) {
        OtpCode otpCode = otpCodeRepository
                .findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), type)
                .orElseThrow(() -> new DomainException("INVALID_OTP", "No active OTP found", 400));

        if (!otpCode.isValid()) {
            throw new DomainException("INVALID_OTP", "OTP code has expired", 400);
        }

        if (otpCode.getFailedAttempts() >= maxAttempts) {
            otpCode.markAsUsed();
            otpCodeRepository.save(otpCode);
            throw new DomainException("OTP_LOCKED", "Too many failed attempts. Please request a new OTP.", 429);
        }

        if (!otpCode.getCode().equals(code)) {
            otpCode.incrementFailedAttempts();
            if (otpCode.getFailedAttempts() >= maxAttempts) {
                otpCode.markAsUsed();
            }
            otpCodeRepository.save(otpCode);
            throw new DomainException("INVALID_OTP", "Invalid OTP code", 400);
        }

        otpCode.markAsUsed();
        return otpCodeRepository.save(otpCode);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(secureRandom.nextInt(10)); // 0-9
        }
        return sb.toString();
    }
}
