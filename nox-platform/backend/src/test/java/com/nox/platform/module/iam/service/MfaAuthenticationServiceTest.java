package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMfaBackupCodeRepository userMfaBackupCodeRepository;

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private MfaService mfaService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private MfaAuthenticationService mfaAuthenticationService;

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

        ReflectionTestUtils.setField(mfaAuthenticationService, "backupCodesCount", 10);
    }

    @Test
    void setupMfa_whenValid_returnsSecretAndUri() {
        when(userRepository.findByEmail(setupUser.getEmail())).thenReturn(Optional.of(setupUser));
        when(mfaService.generateSecretKey()).thenReturn("new-mfa-secret");
        when(mfaService.getQrCodeUri("new-mfa-secret", "test@nox.com")).thenReturn("otpauth://totp/...");

        var result = mfaAuthenticationService.setupMfa(setupUser.getEmail());

        assertEquals("new-mfa-secret", result.secret());
        assertEquals("otpauth://totp/...", result.qrCodeUri());
        assertEquals("new-mfa-secret", setupUser.getSecurity().getTempMfaSecret());
        verify(userRepository).save(setupUser);
    }

    @Test
    void enableMfa_whenValidCode_enablesMfaAndGeneratesBackupCodes() {
        setupUser.getSecurity().setTempMfaSecret("temp-secret");
        when(userRepository.findByEmail(setupUser.getEmail())).thenReturn(Optional.of(setupUser));
        when(mfaService.verifyCode("temp-secret", 123456)).thenReturn(true);

        var result = mfaAuthenticationService.enableMfa(setupUser.getEmail(), 123456);

        assertTrue(setupUser.getSecurity().isMfaEnabled());
        assertEquals("temp-secret", setupUser.getSecurity().getMfaSecret());
        assertNull(setupUser.getSecurity().getTempMfaSecret());
        assertEquals(10, result.size());
        verify(userRepository).save(setupUser);
        verify(userMfaBackupCodeRepository, times(10)).save(any(UserMfaBackupCode.class));
    }

    @Test
    void enableMfa_whenSetupNotInitiated_thenThrowsException() {
        when(userRepository.findByEmail(setupUser.getEmail())).thenReturn(Optional.of(setupUser));
        // tempMfaSecret is natively null

        DomainException ex = assertThrows(DomainException.class,
                () -> mfaAuthenticationService.enableMfa(setupUser.getEmail(), 123456));

        assertEquals("MFA_SETUP_REQUIRED", ex.getCode());
    }

    @Test
    void verifyMfa_whenInvalidClaim_thenThrowsException() {
        when(jwtService.extractUsername("malicious-token")).thenReturn("test@nox.com");
        when(jwtService.extractClaim(eq("malicious-token"), any())).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> mfaAuthenticationService.verifyMfa("malicious-token", 123456, "127.0.0.1", "Mock-Agent"));

        assertEquals("INVALID_MFA_TOKEN", ex.getCode());
    }

    @Test
    void verifyMfa_whenInvalidCode_thenThrowsException() {
        setupUser.getSecurity().setMfaEnabled(true);
        setupUser.getSecurity().setMfaSecret("real-secret");

        when(jwtService.extractUsername("valid-mfa-token")).thenReturn("test@nox.com");
        when(jwtService.extractClaim(eq("valid-mfa-token"), any())).thenReturn(true);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(mfaService.verifyCode("real-secret", 999999)).thenReturn(false); // Invalid code

        DomainException ex = assertThrows(DomainException.class,
                () -> mfaAuthenticationService.verifyMfa("valid-mfa-token", 999999, "127.0.0.1", "Mock-Agent"));

        assertEquals("INVALID_MFA_CODE", ex.getCode());
    }

    @Test
    void verifyMfaBackupCode_whenInvalidCode_thenIncrementsFailedAttempts() {
        setupUser.getSecurity().setMfaEnabled(true);
        setupUser.getSecurity().setFailedMfaAttempts(0);

        when(jwtService.extractUsername("token")).thenReturn("test@nox.com");
        when(jwtService.extractClaim(anyString(), any())).thenReturn(true);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(userMfaBackupCodeRepository.findByUserAndUsedFalse(setupUser)).thenReturn(java.util.List.of()); // No valid
                                                                                                             // codes

        DomainException ex = assertThrows(DomainException.class,
                () -> mfaAuthenticationService.verifyMfaBackupCode("token", "999999", "1.1.1.1", "agent"));

        assertEquals("INVALID_BACKUP_CODE", ex.getCode());
        assertEquals(1, setupUser.getSecurity().getFailedMfaAttempts());
        verify(userSecurityRepository).save(setupUser.getSecurity());
    }

    @Test
    void verifyMfaBackupCode_whenTooManyFailures_thenLocksAccount() {
        setupUser.getSecurity().setMfaEnabled(true);
        setupUser.getSecurity().setFailedMfaAttempts(4); // Max attempts is 5, so 4 + 1 = 5

        when(jwtService.extractUsername("token")).thenReturn("test@nox.com");
        when(jwtService.extractClaim(anyString(), any())).thenReturn(true);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
        when(userMfaBackupCodeRepository.findByUserAndUsedFalse(setupUser)).thenReturn(java.util.List.of()); // No valid
                                                                                                             // codes

        DomainException ex = assertThrows(DomainException.class,
                () -> mfaAuthenticationService.verifyMfaBackupCode("token", "wrong", "1.1.1.1", "agent"));

        assertEquals("ACCOUNT_LOCKED", ex.getCode());
        assertTrue(setupUser.getSecurity().isLocked());
        verify(userSecurityRepository).save(setupUser.getSecurity());
    }

    @Test
    void verifyMfaBackupCode_whenExhaustedCode_thenThrowsException() {
        setupUser.getSecurity().setMfaEnabled(true);

        when(jwtService.extractUsername("valid-mfa-token")).thenReturn("test@nox.com");
        when(jwtService.extractClaim(eq("valid-mfa-token"), any())).thenReturn(true);
        when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));

        UserMfaBackupCode code1 = UserMfaBackupCode.builder().codeHash("hash1").used(false).build();
        UserMfaBackupCode code2 = UserMfaBackupCode.builder().codeHash("hash2").used(false).build();
        when(userMfaBackupCodeRepository.findByUserAndUsedFalse(setupUser)).thenReturn(java.util.List.of(code1, code2));

        // Submit a code resolving to "hash3" which isn't in the active list (e.g.,
        // used=true or non-existent)
        DomainException ex = assertThrows(DomainException.class, () -> mfaAuthenticationService
                .verifyMfaBackupCode("valid-mfa-token", "invalid-plain-code", "127.0.0.1", "Mock-Agent"));

        assertEquals("INVALID_BACKUP_CODE", ex.getCode());
    }
}
