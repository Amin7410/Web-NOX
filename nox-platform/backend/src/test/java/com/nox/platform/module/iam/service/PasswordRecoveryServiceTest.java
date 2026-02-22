package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.event.PasswordResetRequestedEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    private User setupUser;

    @BeforeEach
    void setUp() {
        setupUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@nox.com")
                .fullName("Test User")
                .status(UserStatus.ACTIVE)
                .build();

        UserSecurity security = UserSecurity.builder()
                .user(setupUser)
                .passwordHash("hashed")
                .isPasswordSet(true)
                .build();
        setupUser.setSecurity(security);
    }

    @Test
    void forgotPassword_whenValidEmail_thenSendsEvent() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("987654").type(OtpCode.OtpType.RESET_PASSWORD).build();
        when(otpService.generateOtp(setupUser, OtpCode.OtpType.RESET_PASSWORD)).thenReturn(otp);

        passwordRecoveryService.forgotPassword("test@nox.com");

        ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor = ArgumentCaptor
                .forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("987654", eventCaptor.getValue().getOtpCode());
        assertEquals("test@nox.com", eventCaptor.getValue().getUser().getEmail());
    }

    @Test
    void resetPassword_whenValidOtp_thenUpdatesPassword() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("987654").type(OtpCode.OtpType.RESET_PASSWORD).build();
        when(otpService.validateAndUseOtp(setupUser, "987654", OtpCode.OtpType.RESET_PASSWORD)).thenReturn(otp);
        when(passwordEncoder.encode("newPassword123!")).thenReturn("encoded_new_password");

        passwordRecoveryService.resetPassword("test@nox.com", "987654", "newPassword123!");

        assertEquals("encoded_new_password", setupUser.getSecurity().getPasswordHash());
        assertTrue(setupUser.getSecurity().isPasswordSet());
        verify(userRepository).save(setupUser);
        verify(userSessionRepository).revokeAllUserSessions(setupUser.getId(), "Password Reset");
    }

    @Test
    void resetPassword_whenInvalidOtp_thenThrowsException() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(otpService.validateAndUseOtp(setupUser, "invalid-otp", OtpCode.OtpType.RESET_PASSWORD))
                .thenThrow(new DomainException("INVALID_OTP", "Invalid or expired OTP"));

        DomainException ex = assertThrows(DomainException.class,
                () -> passwordRecoveryService.resetPassword("test@nox.com", "invalid-otp", "newPassword123!"));

        assertEquals("INVALID_OTP", ex.getCode());
    }

    @Test
    void changePassword_whenWrongOldPassword_thenThrowsException() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(passwordEncoder.matches("wrong-old", setupUser.getSecurity().getPasswordHash())).thenReturn(false);

        DomainException ex = assertThrows(DomainException.class,
                () -> passwordRecoveryService.changePassword("test@nox.com", "wrong-old", "newPassword123!"));

        assertEquals("INVALID_PASSWORD", ex.getCode());
    }
}
