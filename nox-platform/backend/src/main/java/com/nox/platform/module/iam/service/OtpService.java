package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.OtpCodeRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 15;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OtpCode generateOtp(User user, OtpCode.OtpType type) {
        // Invalidate any existing unused OTP of the same type for this user
        Optional<OtpCode> existingUnused = otpCodeRepository.findByUser_IdAndTypeAndUsedAtIsNull(user.getId(), type);
        existingUnused.ifPresent(otp -> {
            otp.markAsUsed(); // Soft invalidate
            otpCodeRepository.save(otp);
        });

        String code = generateRandomCode();

        OtpCode otpCode = OtpCode.builder()
                .user(user)
                .code(code)
                .type(type)
                .expiresAt(OffsetDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        return otpCodeRepository.save(otpCode);
    }

    public OtpCode validateAndUseOtp(String code, OtpCode.OtpType type) {
        OtpCode otpCode = otpCodeRepository.findByCodeAndType(code, type)
                .orElseThrow(() -> new DomainException("INVALID_OTP", "Invalid or expired OTP code", 400));

        if (!otpCode.isValid()) {
            throw new DomainException("INVALID_OTP", "Invalid or expired OTP code", 400);
        }

        otpCode.markAsUsed();
        return otpCodeRepository.save(otpCode);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10)); // 0-9
        }
        return sb.toString();
    }
}
