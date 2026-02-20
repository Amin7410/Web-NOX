package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
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
import com.nox.platform.module.iam.domain.OtpCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
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

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private MfaService mfaService;

    @Mock
    private SocialIdentityRepository socialIdentityRepository;

    @Mock
    private UserMfaBackupCodeRepository userMfaBackupCodeRepository;

    @InjectMocks
    private AuthService authService;

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

        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 15);
    }

    @Test
    void registerUser_whenValidInput_thenReturnsSavedUser() {
        // Arrange
        when(userRepository.existsByEmail("test@nox.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_testing_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        OtpCode otp = OtpCode.builder().code("123456").type(OtpCode.OtpType.VERIFY_EMAIL).build();
        when(otpService.generateOtp(any(User.class), eq(OtpCode.OtpType.VERIFY_EMAIL))).thenReturn(otp);

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

        // Verify email sent
        verify(emailService).sendVerificationEmail("test@nox.com", "123456");
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

        AuthService.AuthResult result = authService.authenticate("test@nox.com", "password123", "127.0.0.1",
                "Mock-Agent");

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.token());
        assertEquals("mock-refresh-token", result.refreshToken());
        assertFalse(result.mfaRequired());
        assertEquals(setupUser, result.user());

        // Verify attempts reset to 0
        assertEquals(0, setupUser.getSecurity().getFailedLoginAttempts());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(setupUser);
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void authenticate_whenInvalidCredentials_thenIncrementsFailsAndThrowsException() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.authenticate("test@nox.com", "wrong-password", "127.0.0.1", "Mock-Agent"));

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

        assertThrows(DomainException.class,
                () -> authService.authenticate("test@nox.com", "wrong-password", "127.0.0.1", "Mock-Agent"));

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
                () -> authService.authenticate("test@nox.com", "password123", "127.0.0.1", "Mock-Agent"));

        assertEquals("ACCOUNT_LOCKED", exception.getCode());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_whenAccountNotActive_thenThrowsException() {
        setupUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.authenticate("test@nox.com", "password123", "127.0.0.1", "Mock-Agent"));

        assertEquals("ACCOUNT_NOT_ACTIVE", exception.getCode());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void refreshAccessToken_whenValidToken_thenReturnsNewToken() {
        UserSession session = UserSession.builder()
                .user(setupUser)
                .refreshToken("valid-token")
                .lastActiveAt(OffsetDateTime.now().minusDays(1))
                .expiresAt(OffsetDateTime.now().plusDays(6))
                .build();

        when(userSessionRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(session));
        when(jwtService.generateToken("test@nox.com")).thenReturn("new-jwt-token");

        AuthService.AuthResult result = authService.refreshAccessToken("valid-token", "127.0.0.1", "Mock-Agent");

        assertNotNull(result);
        assertEquals("new-jwt-token", result.token());
        assertEquals("valid-token", result.refreshToken());
        assertFalse(result.mfaRequired());
        assertEquals(setupUser, result.user());
        assertTrue(session.getLastActiveAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
        verify(userSessionRepository).save(session);
    }

    @Test
    void refreshAccessToken_whenExpiredToken_thenThrowsException() {
        UserSession session = UserSession.builder()
                .user(setupUser)
                .refreshToken("expired-token")
                .lastActiveAt(OffsetDateTime.now().minusDays(8))
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(userSessionRepository.findByRefreshToken("expired-token")).thenReturn(Optional.of(session));

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.refreshAccessToken("expired-token", "127.0.0.1", "Mock-Agent"));

        assertEquals("EXP_REFRESH_TOKEN", exception.getCode());
        verify(jwtService, never()).generateToken(any());
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void logout_whenValidToken_thenRevokesSession() {
        UserSession session = UserSession.builder()
                .user(setupUser)
                .refreshToken("valid-token")
                .lastActiveAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();

        when(userSessionRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(session));

        authService.logout("valid-token");

        assertNotNull(session.getRevokedAt());
        assertEquals("User Logged Out", session.getRevokeReason());
        verify(userSessionRepository).save(session);
    }

    @Test
    void verifyEmail_whenValidOtp_thenSetsUserStatusActive() {
        setupUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("123456").type(OtpCode.OtpType.VERIFY_EMAIL).build();
        when(otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL)).thenReturn(otp);

        authService.verifyEmail("test@nox.com", "123456");

        assertEquals(UserStatus.ACTIVE, setupUser.getStatus());
        verify(userRepository).save(setupUser);
    }

    @Test
    void verifyEmail_whenUserAlreadyActive_thenThrowsException() {
        setupUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("123456").type(OtpCode.OtpType.VERIFY_EMAIL).build();
        when(otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL)).thenReturn(otp);

        DomainException exception = assertThrows(DomainException.class,
                () -> authService.verifyEmail("test@nox.com", "123456"));

        assertEquals("USER_ALREADY_ACTIVE", exception.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPassword_whenValidEmail_thenSendsEmail() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("987654").type(OtpCode.OtpType.RESET_PASSWORD).build();
        when(otpService.generateOtp(setupUser, OtpCode.OtpType.RESET_PASSWORD)).thenReturn(otp);

        authService.forgotPassword("test@nox.com");

        verify(emailService).sendPasswordResetEmail("test@nox.com", "987654");
    }

    @Test
    void resetPassword_whenValidOtp_thenUpdatesPassword() {
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        OtpCode otp = OtpCode.builder().user(setupUser).code("987654").type(OtpCode.OtpType.RESET_PASSWORD).build();
        when(otpService.validateAndUseOtp(setupUser, "987654", OtpCode.OtpType.RESET_PASSWORD)).thenReturn(otp);
        when(passwordEncoder.encode("newPassword123!")).thenReturn("encoded_new_password");

        authService.resetPassword("test@nox.com", "987654", "newPassword123!");

        assertEquals("encoded_new_password", setupUser.getSecurity().getPasswordHash());
        assertTrue(setupUser.getSecurity().isPasswordSet());
        verify(userRepository).save(setupUser);
    }
}
