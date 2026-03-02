package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.iam.service.internal.InternalSecurityStateService;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private InternalSecurityStateService internalSecurityStateService;

    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private AuthenticationService authenticationService;

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

        ReflectionTestUtils.setField(authenticationService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authenticationService, "lockoutDurationMinutes", 15);
    }

    @Test
    void authenticate_whenValidCredentials_thenReturnsTokenAndResetsFails() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        AuthenticationService.AuthResult mockResult = new AuthenticationService.AuthResult(setupUser, "mock-jwt-token",
                "mock-refresh-token", false, null);
        when(userSessionService.generateSuccessAuthResult(setupUser, "127.0.0.1", "Mock-Agent")).thenReturn(mockResult);

        setupUser.getSecurity().setFailedLoginAttempts(3);

        AuthenticationService.AuthResult result = authenticationService.authenticate("test@nox.com",
                "password123",
                "127.0.0.1", "Mock-Agent");

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.token());
        assertEquals("mock-refresh-token", result.refreshToken());
        assertFalse(result.mfaRequired());
        assertEquals(setupUser, result.user());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(internalSecurityStateService).resetFailedLogins(setupUser.getId());
    }

    @Test
    void authenticate_whenInvalidCredentials_thenIncrementsFailsAndLocksAccount() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(authenticationManager.authenticate(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("Bad creds"));
        // Return 5 to simulate that the DB-level increment has occurred
        setupUser.getSecurity().setFailedLoginAttempts(4);

        DomainException ex = assertThrows(DomainException.class, () -> authenticationService
                .authenticate("test@nox.com", "wrongpass", "127.0.0.1", "Mock-Agent"));

        assertEquals("INVALID_CREDENTIALS", ex.getCode());
        verify(internalSecurityStateService).incrementFailedLogins(setupUser.getId());
        verify(internalSecurityStateService).lockAccount(eq(setupUser.getId()), any());
    }

    @Test
    void authenticate_whenAccountLocked_thenThrowsException() {
        setupUser.getSecurity().lockAccount(15);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        DomainException ex = assertThrows(DomainException.class, () -> authenticationService
                .authenticate("test@nox.com", "password123", "127.0.0.1", "Mock-Agent"));

        assertEquals("ACCOUNT_LOCKED", ex.getCode());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_whenEmailNotVerified_thenThrowsException() {
        setupUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        DomainException ex = assertThrows(DomainException.class, () -> authenticationService
                .authenticate("test@nox.com", "password123", "127.0.0.1", "Mock-Agent"));

        assertEquals("ACCOUNT_NOT_ACTIVE", ex.getCode());
    }
}
