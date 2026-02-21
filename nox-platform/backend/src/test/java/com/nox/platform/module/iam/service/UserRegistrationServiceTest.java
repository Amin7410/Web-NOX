package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.event.UserRegisteredEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private User setupUser;

    @BeforeEach
    void setUp() {
        setupUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@nox.com")
                .fullName("Test User")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();
    }

    @Test
    void registerUser_whenValidInput_thenReturnsSavedUserAndPublishesEvent() {
        when(userRepository.existsByEmail("test@nox.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        OtpCode otp = OtpCode.builder().code("123456").type(OtpCode.OtpType.VERIFY_EMAIL).build();
        when(otpService.generateOtp(any(User.class), eq(OtpCode.OtpType.VERIFY_EMAIL))).thenReturn(otp);

        User result = userRegistrationService.registerUser("test@nox.com", "password123", "Test User");

        assertNotNull(result);
        assertEquals("test@nox.com", result.getEmail());

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("123456", eventCaptor.getValue().getOtpCode());
        assertEquals("test@nox.com", eventCaptor.getValue().getUser().getEmail());
    }

    @Test
    void verifyEmail_whenValidOtp_thenSetsUserStatusActive() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("123456").type(OtpCode.OtpType.VERIFY_EMAIL).build();
        when(otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL)).thenReturn(otp);

        userRegistrationService.verifyEmail("test@nox.com", "123456");

        assertEquals(UserStatus.ACTIVE, setupUser.getStatus());
        verify(userRepository).save(setupUser);
    }

    @Test
    void registerUser_whenDuplicateEmail_thenThrowsException() {
        when(userRepository.existsByEmail("duplicate@nox.com")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> userRegistrationService.registerUser("duplicate@nox.com", "password123", "Test User"));

        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void verifyEmail_whenUserAlreadyActive_thenThrowsException() {
        setupUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> userRegistrationService.verifyEmail("test@nox.com", "123456"));

        assertEquals("USER_ALREADY_ACTIVE", ex.getCode());
    }
}
