package com.nox.platform.module.iam.service.registration;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.event.UserRegisteredEvent;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.service.OtpService;
import com.nox.platform.module.iam.service.UserRegistrationService;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationService Unit Tests")
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OtpService otpService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private UserRegistrationService registrationService;

    private final String email = "test@example.com";
    private final String password = "password123";
    private final String fullName = "John Doe";
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.now()).thenReturn(now);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
    }

    @Nested
    @DisplayName("User Registration Scenarios")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldRegisterNewUser() {
            // Given
            when(userRepository.findByEmailIncludeDeleted(email.toLowerCase())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            
            OtpCode otp = OtpCode.builder().code("123456").build();
            when(otpService.generateOtp(any(User.class), eq(OtpCode.OtpType.VERIFY_EMAIL))).thenReturn(otp);

            // When
            User result = registrationService.registerUser(email, password, fullName);

            // Then
            assertThat(result.getEmail()).isEqualTo(email.toLowerCase());
            assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
            assertThat(result.getSecurity()).isNotNull();
            
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
        }

        @Test
        @DisplayName("Should allow re-registration if account is still pending verification")
        void shouldAllowReRegistrationForPendingUser() {
            // Given
            User pendingUser = User.builder()
                    .email(email)
                    .status(UserStatus.PENDING_VERIFICATION)
                    .build();
            pendingUser.initializeTimestamps(now.minusDays(1));
            
            // Link a security object to avoid NPE
            com.nox.platform.module.iam.domain.UserSecurity security = 
                    com.nox.platform.module.iam.domain.UserSecurity.builder().user(pendingUser).build();
            pendingUser.linkSecurity(security);
            
            when(userRepository.findByEmailIncludeDeleted(email)).thenReturn(Optional.of(pendingUser));
            when(otpService.generateOtp(any(User.class), any())).thenReturn(OtpCode.builder().code("654321").build());

            // When
            registrationService.registerUser(email, password, "New Name");

            // Then
            assertThat(pendingUser.getFullName()).isEqualTo("New Name");
            verify(userRepository).save(pendingUser);
            verify(otpService).generateOtp(eq(pendingUser), eq(OtpCode.OtpType.VERIFY_EMAIL));
        }

        @Test
        @DisplayName("Should throw exception if email is already active")
        void shouldThrowIfEmailExists() {
            // Given
            User activeUser = User.builder().email(email).status(UserStatus.ACTIVE).build();
            when(userRepository.findByEmailIncludeDeleted(email)).thenReturn(Optional.of(activeUser));

            // When & Then
            assertThatThrownBy(() -> registrationService.registerUser(email, password, fullName))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "EMAIL_ALREADY_EXISTS");
        }

        @Test
        @DisplayName("Should prevent registration if account was previously deleted")
        void shouldPreventRegistrationForDeletedUser() {
            // Given
            User deletedUser = User.builder().email(email).status(UserStatus.DELETED).build();
            when(userRepository.findByEmailIncludeDeleted(email)).thenReturn(Optional.of(deletedUser));

            // When & Then
            assertThatThrownBy(() -> registrationService.registerUser(email, password, fullName))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "EMAIL_ALREADY_EXISTS")
                    .hasMessageContaining("deleted");
        }
    }

    @Nested
    @DisplayName("Email Verification Scenarios")
    class VerificationTests {

        @Test
        @DisplayName("Should successfully verify email with valid OTP")
        void shouldVerifyEmail() {
            // Given
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .status(UserStatus.PENDING_VERIFICATION)
                    .build();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When
            registrationService.verifyEmail(email, "123456");

            // Then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(otpService).validateAndUseOtp(user, "123456", OtpCode.OtpType.VERIFY_EMAIL);
            verify(userRepository).save(user);
            verify(eventPublisher).publishEvent(any(com.nox.platform.shared.event.UserCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception if user is already verified")
        void shouldThrowIfAlreadyVerified() {
            // Given
            User user = User.builder().email(email).status(UserStatus.ACTIVE).build();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When & Then
            assertThatThrownBy(() -> registrationService.verifyEmail(email, "123456"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "USER_ALREADY_ACTIVE");
        }
    }
}
