package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.OtpCode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.OtpCodeRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @InjectMocks
    private OtpService otpService;

    private User setupUser;

    @BeforeEach
    void setUp() {
        setupUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@nox.com")
                .fullName("Test User")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        ReflectionTestUtils.setField(otpService, "otpLength", 6);
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 15);
    }

    @Test
    void generateOtp_invalidatesPreviousAndReturnsNewOtp() {
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(i -> i.getArguments()[0]);

        OtpCode result = otpService.generateOtp(setupUser, OtpCode.OtpType.VERIFY_EMAIL);

        assertNotNull(result);
        assertEquals(setupUser, result.getUser());
        assertEquals(OtpCode.OtpType.VERIFY_EMAIL, result.getType());
        assertEquals(6, result.getCode().length());
        assertTrue(result.getExpiresAt().isAfter(OffsetDateTime.now().plusMinutes(14)));

        verify(otpCodeRepository).invalidatePreviousOtps(setupUser.getId(), OtpCode.OtpType.VERIFY_EMAIL);
        verify(otpCodeRepository).save(any(OtpCode.class));
    }

    @Test
    void validateAndUseOtp_whenValid_returnsUsedOtp() {
        OtpCode otpCode = OtpCode.builder()
                .user(setupUser)
                .code("123456")
                .type(OtpCode.OtpType.VERIFY_EMAIL)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .failedAttempts(0)
                .build();

        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.of(otpCode));
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(i -> i.getArguments()[0]);

        OtpCode result = otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL);

        assertTrue(result.isUsed());
        assertNotNull(result.getUsedAt());
        assertEquals(0, result.getFailedAttempts());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void validateAndUseOtp_whenExpired_throwsException() {
        OtpCode otpCode = OtpCode.builder()
                .user(setupUser)
                .code("123456")
                .type(OtpCode.OtpType.VERIFY_EMAIL)
                .expiresAt(OffsetDateTime.now().minusMinutes(1)) // Expired
                .failedAttempts(0)
                .build();

        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.of(otpCode));

        DomainException exception = assertThrows(DomainException.class,
                () -> otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL));

        assertEquals("INVALID_OTP", exception.getCode());
        assertEquals("OTP code has expired", exception.getMessage());
        verify(otpCodeRepository, never()).save(any());
    }

    @Test
    void validateAndUseOtp_whenInvalidCode_incrementsFailedAttemptsAndThrowsException() {
        OtpCode otpCode = OtpCode.builder()
                .user(setupUser)
                .code("123456")
                .type(OtpCode.OtpType.VERIFY_EMAIL)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .failedAttempts(2)
                .build();

        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.of(otpCode));
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(i -> i.getArguments()[0]);

        DomainException exception = assertThrows(DomainException.class,
                () -> otpService.validateAndUseOtp(setupUser, "654321", OtpCode.OtpType.VERIFY_EMAIL));

        assertEquals("INVALID_OTP", exception.getCode());
        assertEquals(3, otpCode.getFailedAttempts());
        assertFalse(otpCode.isUsed());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void validateAndUseOtp_whenInvalidCodeReaches5Attempts_marksAsUsedAndThrowsException() {
        OtpCode otpCode = OtpCode.builder()
                .user(setupUser)
                .code("123456")
                .type(OtpCode.OtpType.VERIFY_EMAIL)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .failedAttempts(4)
                .build();

        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.of(otpCode));
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(i -> i.getArguments()[0]);

        DomainException exception = assertThrows(DomainException.class,
                () -> otpService.validateAndUseOtp(setupUser, "654321", OtpCode.OtpType.VERIFY_EMAIL));

        assertEquals("INVALID_OTP", exception.getCode());
        assertEquals(5, otpCode.getFailedAttempts());
        assertTrue(otpCode.isUsed()); // Marked as used because it reached 5 attempts
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void validateAndUseOtp_whenAlreadyLocked_throwsLockedException() {
        OtpCode otpCode = OtpCode.builder()
                .user(setupUser)
                .code("123456")
                .type(OtpCode.OtpType.VERIFY_EMAIL)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .failedAttempts(5)
                .build();

        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.of(otpCode));
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(i -> i.getArguments()[0]);

        DomainException exception = assertThrows(DomainException.class,
                () -> otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL));

        assertEquals("OTP_LOCKED", exception.getCode());
        assertTrue(otpCode.isUsed());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void validateAndUseOtp_whenNotFound_throwsException() {
        when(otpCodeRepository.findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(setupUser.getId(),
                OtpCode.OtpType.VERIFY_EMAIL))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> otpService.validateAndUseOtp(setupUser, "123456", OtpCode.OtpType.VERIFY_EMAIL));

        assertEquals("INVALID_OTP", exception.getCode());
        assertEquals("No active OTP found", exception.getMessage());
    }
}
