package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.event.UserRegisteredEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.event.UserCreatedEvent;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

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
    private final TimeProvider timeProvider;

    @Transactional
    public User registerUser(String email, String plaintextPassword, String fullName) {
        email = email.trim().toLowerCase();
        OffsetDateTime now = timeProvider.now();
        User existingUser = userRepository.findByEmailIncludeDeleted(email).orElse(null);
        if (existingUser != null) {
            if (existingUser.getStatus() == UserStatus.DELETED) {
                // DO NOT REACTIVATE - Prevent Zombie Accounts explicitly
                throw new DomainException("EMAIL_ALREADY_EXISTS", "A user with this email has been deleted. Please contact support.");
            }

            if (existingUser.getStatus() != UserStatus.PENDING_VERIFICATION) {
                throw new DomainException("EMAIL_ALREADY_EXISTS", "A user with this email already exists");
            }

            // If pending, we allow re-sending registration OTP override
            existingUser.setFullName(fullName);
            existingUser.getSecurity().updatePassword(passwordEncoder.encode(plaintextPassword), now);
            userRepository.save(existingUser);

            OtpCode otp = otpService.generateOtp(existingUser, OtpCode.OtpType.VERIFY_EMAIL);
            eventPublisher.publishEvent(new UserRegisteredEvent(this, existingUser, otp.getCode()));
            return existingUser;
        }

        User user = User.create(email, fullName, UserStatus.PENDING_VERIFICATION, now);
        UserSecurity security = UserSecurity.create(user, passwordEncoder.encode(plaintextPassword), true, now);
        user.linkSecurity(security);
        user = userRepository.save(user);

        OtpCode otp = otpService.generateOtp(user, OtpCode.OtpType.VERIFY_EMAIL);

        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, otp.getCode()));

        return user;
    }

    @Transactional
    public void verifyEmail(String email, String otpCode) {
        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        otpService.validateAndUseOtp(user, otpCode, OtpCode.OtpType.VERIFY_EMAIL);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new DomainException("USER_ALREADY_ACTIVE", "This account is already verified.");
        }

        user.verifyEmail();
        userRepository.save(user);

        // Publish event to trigger warehouse provisioning for the new active user
        eventPublisher.publishEvent(new UserCreatedEvent(user.getId(), user.getEmail()));
    }
}

