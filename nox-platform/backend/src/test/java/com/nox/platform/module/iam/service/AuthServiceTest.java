package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.domain.UserSession;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.OffsetDateTime;
import java.util.Optional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User setupUser;

    @BeforeEach
    void setUp() {
        setupUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@nox.com")
                .fullName("Test User")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();
        UserSecurity security = UserSecurity.builder()
                .user(setupUser)
                .passwordHash("hashed")
                .isPasswordSet(true)
                .build();
        setupUser.setSecurity(security);
    }

    @Test
    void registerUser_whenValidInput_thenReturnsSavedUser() {
        // Arrange
        when(userRepository.existsByEmail("test@nox.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_testing_password");
        when(userRepository.save(any(User.class))).thenReturn(setupUser);

        // Act
        User result = authService.registerUser("test@nox.com", "password123", "Test User");

        // Assert
        assertNotNull(result);
        assertEquals("test@nox.com", result.getEmail());

        // Verify correct object was created
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("test@nox.com", capturedUser.getEmail());
        assertEquals("Test User", capturedUser.getFullName());
        assertEquals(UserStatus.PENDING_VERIFICATION, capturedUser.getStatus());

        // Verify Security bindings
        UserSecurity security = capturedUser.getSecurity();
        assertNotNull(security);
        assertEquals("encoded_testing_password", security.getPasswordHash());
        assertTrue(security.isPasswordSet());
        assertEquals(capturedUser, security.getUser());
    }

    @Test
    void registerUser_whenEmailAlreadyExists_thenThrowsDomainException() {
        // Arrange
        when(userRepository.existsByEmail("duplicate@nox.com")).thenReturn(true);

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class,
                () -> authService.registerUser("duplicate@nox.com", "password123", "Dupe User"));

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_whenValidCredentials_thenReturnsTokenAndResetsFails() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(jwtService.generateToken("test@nox.com")).thenReturn("mock-jwt-token");
        when(jwtService.generateRefreshToken()).thenReturn("mock-refresh-token");
        setupUser.getSecurity().setFailedLoginAttempts(3); // simulate prior fails

        AuthService.AuthResult result = authService.authenticate("test@nox.com", "password123");

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.token());
        assertEquals("mock-refresh-token", result.refreshToken());
        assertEquals(setupUser, result.user());

        // Verify attempts reset to 0
        assertEquals(0, setupUser.getSecurity().getFailedLoginAttempts());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).save(setupUser);
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void authenticate_whenInvalidCredentials_thenIncrementsFailsAndThrowsException() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.authenticate("test@nox.com", "wrong-password"));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        assertEquals(1, setupUser.getSecurity().getFailedLoginAttempts());
        assertNull(setupUser.getSecurity().getLockedUntil());
        verify(userRepository).save(setupUser);
    }

    @Test
    void authenticate_when5FailedAttempts_thenLocksAccount() {
        setupUser.getSecurity().setFailedLoginAttempts(4);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        assertThrows(DomainException.class, () -> authService.authenticate("test@nox.com", "wrong-password"));

        assertEquals(5, setupUser.getSecurity().getFailedLoginAttempts());
        assertNotNull(setupUser.getSecurity().getLockedUntil());
        assertTrue(setupUser.getSecurity().isLocked());
        verify(userRepository).save(setupUser);
    }

    @Test
    void authenticate_whenAccountLocked_thenThrowsLockedExceptionWithoutAuth() {
        setupUser.getSecurity().lockAccount(15);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.authenticate("test@nox.com", "password123"));

        assertEquals("ACCOUNT_LOCKED", exception.getCode());
        verify(authenticationManager, never()).authenticate(any());
    }
}
