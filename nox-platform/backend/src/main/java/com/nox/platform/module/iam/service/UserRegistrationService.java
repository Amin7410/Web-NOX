package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.event.UserRegisteredEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User registerUser(String email, String plaintextPassword, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("EMAIL_ALREADY_EXISTS", "A user with this email already exists", 400);
        }

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        String hashedPassword = passwordEncoder.encode(plaintextPassword);
        UserSecurity security = UserSecurity.builder()
                .user(user)
                .passwordHash(hashedPassword)
                .isPasswordSet(true)
                .build();
        user.setSecurity(security);
        user = userRepository.save(user);

        OtpCode otp = otpService.generateOtp(user, OtpCode.OtpType.VERIFY_EMAIL);

        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, otp.getCode()));

        return user;
    }

    @Transactional
    public void verifyEmail(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        otpService.validateAndUseOtp(user, otpCode, OtpCode.OtpType.VERIFY_EMAIL);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new DomainException("USER_ALREADY_ACTIVE", "This account is already verified.", 400);
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
