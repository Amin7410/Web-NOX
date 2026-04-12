package com.nox.platform.module.iam.service.authentication;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.UserSessionService;
import com.nox.platform.module.iam.service.abstraction.TokenProvider;
import com.nox.platform.module.iam.service.internal.InternalSecurityStateService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private InternalSecurityStateService internalSecurityStateService;
    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private UserSecurity security;
    private final String email = "auth@example.com";
    private final String password = "password123";
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authenticationService, "lockoutDurationMinutes", 15);

        security = UserSecurity.builder()
                .failedLoginAttempts(0)
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();
        user.linkSecurity(security);

        lenient().when(timeProvider.now()).thenReturn(now);
    }

    @Nested
    @DisplayName("Main Login Scenarios")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login and generate tokens when MFA is disabled")
        void shouldLoginSuccessfully() {
            // Given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userSessionService.generateSuccessAuthResult(any(), any(), any()))
                    .thenReturn(new AuthenticationService.AuthResult(user, "jwt", "refresh", false, null));

            // When
            AuthenticationService.AuthResult result = authenticationService.authenticate(email, password, "127.0.0.1", "agent");

            // Then
            assertThat(result.token()).isEqualTo("jwt");
            assertThat(result.mfaRequired()).isFalse();
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(internalSecurityStateService).resetFailedLogins(user.getId());
        }

        @Test
        @DisplayName("Should return MFA pending token when user has MFA enabled")
        void shouldRequireMfa() {
            // Given
            ReflectionTestUtils.setField(security, "mfaEnabled", true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(tokenProvider.generateToken(anyMap(), eq(email))).thenReturn("mfa_temp_token");

            // When
            AuthenticationService.AuthResult result = authenticationService.authenticate(email, password, "127.0.0.1", "agent");

            // Then
            assertThat(result.mfaRequired()).isTrue();
            assertThat(result.mfaToken()).isEqualTo("mfa_temp_token");
            assertThat(result.token()).isNull();
        }

        @Test
        @DisplayName("Should throw exception if account is locked")
        void shouldThrowIfLocked() {
            // Given
            ReflectionTestUtils.setField(security, "lockedUntil", now.plusMinutes(10));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(email, password, "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "ACCOUNT_LOCKED");
        }

        @Test
        @DisplayName("Should throw exception if account is not active")
        void shouldThrowIfInactive() {
            // Given
            ReflectionTestUtils.setField(user, "status", UserStatus.PENDING_VERIFICATION);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(email, password, "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "ACCOUNT_NOT_ACTIVE");
        }
    }

    @Nested
    @DisplayName("Security & Lockout Scenarios")
    class SecurityTests {

        @Test
        @DisplayName("Should increment failure count and throw exception on bad credentials")
        void shouldHandleFailedLogin() {
            // Given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            doThrow(new BadCredentialsException("Bad")).when(authenticationManager).authenticate(any());

            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(email, password, "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "INVALID_CREDENTIALS");

            verify(internalSecurityStateService).incrementFailedLogins(user.getId());
        }

        @Test
        @DisplayName("Should lock account after maximum failed attempts (5)")
        void shouldLockAccountAfterThreshold() {
            // Given
            ReflectionTestUtils.setField(security, "failedLoginAttempts", 4); // 4 previous fails, current one makes it 5
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            doThrow(new BadCredentialsException("Bad")).when(authenticationManager).authenticate(any());

            // When & Then
            assertThatThrownBy(() -> authenticationService.authenticate(email, password, "ip", "ua"));

            verify(internalSecurityStateService).lockAccount(eq(user.getId()), any(OffsetDateTime.class));
        }
    }
}
