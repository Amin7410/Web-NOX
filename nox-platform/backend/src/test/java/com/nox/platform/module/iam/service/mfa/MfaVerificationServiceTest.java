package com.nox.platform.module.iam.service.mfa;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.UserMfaBackupCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.MfaService;
import com.nox.platform.module.iam.service.MfaVerificationService;
import com.nox.platform.module.iam.service.UserSessionService;
import com.nox.platform.module.iam.service.abstraction.TokenProvider;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MfaVerificationService Unit Tests")
class MfaVerificationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMfaBackupCodeRepository backupCodeRepository;
    @Mock
    private MfaService mfaService;
    @Mock
    private UserSecurityRepository userSecurityRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private MfaVerificationService mfaVerificationService;

    private User user;
    private UserSecurity security;
    private final String email = "mfa@example.com";
    private final String mfaToken = "valid_mfa_token";
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        security = UserSecurity.builder()
                .mfaEnabled(true)
                .mfaSecret("SECRET")
                .failedMfaAttempts(0)
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();
        user.linkSecurity(security);

        lenient().when(timeProvider.now()).thenReturn(now);
        lenient().when(tokenProvider.extractUsername(mfaToken)).thenReturn(email);
        lenient().when(tokenProvider.extractClaim(eq(mfaToken), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("Standard Code Verification")
    class CodeVerificationTests {

        @Test
        @DisplayName("Should successfully verify correct MFA code")
        void shouldVerifyCorrectCode() {
            // Given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(mfaService.verifyCode(anyString(), anyInt())).thenReturn(true);
            when(userSessionService.generateSuccessAuthResult(any(), any(), any()))
                    .thenReturn(new AuthenticationService.AuthResult(user, "jwt", "refresh", false, null));

            // When
            AuthenticationService.AuthResult result = mfaVerificationService.verifyMfa(mfaToken, 123456, "ip", "ua");

            // Then
            assertThat(result.token()).isEqualTo("jwt");
            verify(userSecurityRepository).save(security);
            assertThat(security.getFailedMfaAttempts()).isZero();
        }

        @Test
        @DisplayName("Should throw exception and increment failure on invalid MFA code")
        void shouldHandleInvalidCode() {
            // Given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(mfaService.verifyCode(anyString(), anyInt())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> mfaVerificationService.verifyMfa(mfaToken, 111111, "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "INVALID_MFA_CODE");

            assertThat(security.getFailedMfaAttempts()).isEqualTo(1);
            verify(userSecurityRepository).save(security);
        }

        @Test
        @DisplayName("Should lock account after 5 failed MFA attempts")
        void shouldLockAfterFailedAttempts() {
            // Given
            ReflectionTestUtils.setField(security, "failedMfaAttempts", 4);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(mfaService.verifyCode(anyString(), anyInt())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> mfaVerificationService.verifyMfa(mfaToken, 111111, "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "ACCOUNT_LOCKED");

            assertThat(security.isLocked(now)).isTrue();
        }
    }

    @Nested
    @DisplayName("Backup Code Verification")
    class BackupCodeTests {

        @Test
        @DisplayName("Should successfully verify valid backup code")
        void shouldVerifyBackupCode() {
            // Given
            String rawCode = "ABCDE-12345";
            String hashedCode = DigestUtils.sha256Hex(rawCode);
            UserMfaBackupCode backupCode = UserMfaBackupCode.builder()
                    .user(user)
                    .codeHash(hashedCode)
                    .used(false)
                    .build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(backupCodeRepository.findByUserAndUsedFalse(user)).thenReturn(List.of(backupCode));
            when(userSessionService.generateSuccessAuthResult(any(), any(), any()))
                    .thenReturn(new AuthenticationService.AuthResult(user, "jwt", "refresh", false, null));

            // When
            AuthenticationService.AuthResult result = mfaVerificationService.verifyMfaBackupCode(mfaToken, rawCode, "ip", "ua");

            // Then
            assertThat(result.token()).isEqualTo("jwt");
            assertThat(backupCode.isUsed()).isTrue();
            verify(backupCodeRepository).save(backupCode);
        }

        @Test
        @DisplayName("Should throw exception on invalid backup code")
        void shouldHandleInvalidBackupCode() {
            // Given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(backupCodeRepository.findByUserAndUsedFalse(user)).thenReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> mfaVerificationService.verifyMfaBackupCode(mfaToken, "WRONG", "ip", "ua"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "INVALID_BACKUP_CODE");
        }
    }
}
