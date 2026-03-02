package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMfaBackupCodeRepository userMfaBackupCodeRepository;

    @Mock
    private MfaService mfaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private MfaManagementService mfaManagementService;

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

        ReflectionTestUtils.setField(mfaManagementService, "backupCodesCount", 10);
    }

    @Test
    void setupMfa_whenValid_returnsSecretAndUri() {
        when(userRepository.findByEmail(setupUser.getEmail())).thenReturn(Optional.of(setupUser));
        when(mfaService.generateSecretKey()).thenReturn("new-mfa-secret");
        when(mfaService.getQrCodeUri("new-mfa-secret", "test@nox.com")).thenReturn("otpauth://totp/...");

        var result = mfaManagementService.setupMfa(setupUser.getEmail());

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

        var result = mfaManagementService.enableMfa(setupUser.getEmail(), 123456);

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
                () -> mfaManagementService.enableMfa(setupUser.getEmail(), 123456));

        assertEquals("MFA_SETUP_REQUIRED", ex.getCode());
    }

    @Test
    void disableMfa_whenValidPassword_disablesMfa() {
        setupUser.getSecurity().setMfaEnabled(true);
        when(userRepository.findByEmail(setupUser.getEmail())).thenReturn(Optional.of(setupUser));

        UserDetails mockUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(setupUser.getEmail())).thenReturn(mockUserDetails);
        when(mockUserDetails.getPassword()).thenReturn("hashed");
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        mfaManagementService.disableMfa(setupUser.getEmail(), "password123");

        assertFalse(setupUser.getSecurity().isMfaEnabled());
        assertNull(setupUser.getSecurity().getMfaSecret());
        verify(userRepository).save(setupUser);
        verify(userMfaBackupCodeRepository).deleteByUser(setupUser);
    }
}
