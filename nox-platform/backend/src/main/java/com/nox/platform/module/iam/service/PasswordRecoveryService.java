package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.event.PasswordResetRequestedEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() != com.nox.platform.module.iam.domain.UserStatus.ACTIVE) {
                return;
            }
            OtpCode otp = otpService.generateOtp(user, OtpCode.OtpType.RESET_PASSWORD);
            eventPublisher.publishEvent(new PasswordResetRequestedEvent(this, user, otp.getCode()));
        });
    }

    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        otpService.validateAndUseOtp(user, otpCode, OtpCode.OtpType.RESET_PASSWORD);

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.getSecurity().setPasswordHash(hashedPassword);
        user.getSecurity().setPasswordSet(true);
        userRepository.save(user);

        userSessionRepository.revokeAllUserSessions(user.getId(), "Password Reset");
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (!user.getSecurity().isPasswordSet()
                || !passwordEncoder.matches(oldPassword, user.getSecurity().getPasswordHash())) {
            throw new DomainException("INVALID_PASSWORD", "Old password is not correct", 400);
        }

        user.getSecurity().setPasswordHash(passwordEncoder.encode(newPassword));
        user.getSecurity().setLastPasswordChange(OffsetDateTime.now());
        userRepository.save(user);

        userSessionRepository.revokeAllUserSessions(user.getId(), "Password Changed");
    }
}
