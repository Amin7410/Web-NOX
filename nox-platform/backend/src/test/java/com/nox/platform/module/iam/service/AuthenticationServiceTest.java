package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
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

import java.time.OffsetDateTime;
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
        private UserSessionRepository userSessionRepository;

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtService jwtService;

        @Mock
        private SocialIdentityRepository socialIdentityRepository;

        @Mock
        private SocialAuthVerificationService socialAuthVerificationService;

        @Mock
        private UserSecurityRepository userSecurityRepository;

        @Mock
        private InternalSecurityStateService internalSecurityStateService;

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
                ReflectionTestUtils.setField(authenticationService, "refreshTokenExpirationDays", 7);
        }

        @Test
        void authenticate_whenValidCredentials_thenReturnsTokenAndResetsFails() {
                when(userRepository.findByEmail("test@nox.com")).thenReturn(Optional.of(setupUser));
                when(jwtService.generateToken("test@nox.com")).thenReturn("mock-jwt-token");
                when(jwtService.generateRefreshToken()).thenReturn("mock-refresh-token");
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
                verify(userSessionRepository).save(any(UserSession.class));
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

        @Test
        void refreshAccessToken_whenValidToken_thenReturnsNewToken() {
                String hashedToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex("valid-token");
                UserSession session = UserSession.builder()
                                .user(setupUser)
                                .refreshToken(hashedToken)
                                .lastActiveAt(OffsetDateTime.now().minusDays(1))
                                .expiresAt(OffsetDateTime.now().plusDays(6))
                                .build();

                when(userSessionRepository.findByRefreshToken(hashedToken)).thenReturn(Optional.of(session));
                when(jwtService.generateToken("test@nox.com")).thenReturn("new-jwt-token");
                when(jwtService.generateRefreshToken()).thenReturn("new-refresh-token");

                AuthenticationService.AuthResult result = authenticationService.refreshAccessToken("valid-token",
                                "127.0.0.1",
                                "Mock-Agent");

                assertNotNull(result);
                assertEquals("new-jwt-token", result.token());
                assertEquals("new-refresh-token", result.refreshToken());
                assertFalse(result.mfaRequired());
                assertEquals(setupUser, result.user());
                assertNotNull(session.getRevokedAt());
                assertEquals("Token Rotated", session.getRevokeReason());
                verify(userSessionRepository, times(2)).save(any(UserSession.class));
        }

        @Test
        void refreshAccessToken_whenTokenExpired_thenThrowsException() {
                String hashedToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex("expired-token");
                UserSession session = UserSession.builder()
                                .user(setupUser)
                                .refreshToken(hashedToken)
                                .lastActiveAt(OffsetDateTime.now().minusDays(10))
                                .expiresAt(OffsetDateTime.now().minusDays(3)) // Expired
                                .build();

                when(userSessionRepository.findByRefreshToken(hashedToken)).thenReturn(Optional.of(session));

                DomainException ex = assertThrows(DomainException.class, () -> authenticationService
                                .refreshAccessToken("expired-token", "127.0.0.1", "Mock-Agent"));

                assertEquals("COMPROMISED_TOKEN", ex.getCode());
                verify(userSessionRepository).revokeAllUserSessions(eq(setupUser.getId()), anyString());
        }

        @Test
        void refreshAccessToken_whenUserNotActive_thenThrowsException() {
                String hashedToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex("valid-token");
                UserSession session = UserSession.builder()
                                .user(setupUser)
                                .refreshToken(hashedToken)
                                .lastActiveAt(OffsetDateTime.now().minusDays(1))
                                .expiresAt(OffsetDateTime.now().plusDays(6))
                                .build();

                setupUser.setStatus(UserStatus.BANNED);

                when(userSessionRepository.findByRefreshToken(hashedToken)).thenReturn(Optional.of(session));

                DomainException ex = assertThrows(DomainException.class, () -> authenticationService
                                .refreshAccessToken("valid-token", "127.0.0.1", "Mock-Agent"));

                assertEquals("ACCOUNT_NOT_ACTIVE", ex.getCode());
                verify(jwtService, never()).generateToken(anyString());
        }

        @Test
        void logout_whenInvalidOwner_thenThrowsException() {
                String hashedToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex("valid-token");
                UserSession session = UserSession.builder()
                                .user(setupUser)
                                .refreshToken(hashedToken)
                                .build();

                when(userSessionRepository.findByRefreshToken(hashedToken)).thenReturn(Optional.of(session));

                DomainException ex = assertThrows(DomainException.class,
                                () -> authenticationService.logout("valid-token", "attacker@nox.com"));

                assertEquals("UNAUTHORIZED_LOGOUT", ex.getCode());
        }

        @Test
        void socialLogin_whenUserBanned_thenThrowsException() {
                java.util.Map<String, Object> verifiedData = new java.util.HashMap<>();
                verifiedData.put("providerId", "google-id");
                verifiedData.put("email", "test@nox.com");
                verifiedData.put("fullName", "Full Name");
                verifiedData.put("rawProfile", new java.util.HashMap<>());

                when(socialAuthVerificationService.verifyToken(anyString(), anyString())).thenReturn(verifiedData);
                when(socialIdentityRepository.findByProviderAndProviderId(anyString(), anyString()))
                                .thenReturn(Optional.of(com.nox.platform.module.iam.domain.SocialIdentity.builder()
                                                .user(setupUser)
                                                .build()));
                setupUser.setStatus(UserStatus.BANNED);

                DomainException ex = assertThrows(DomainException.class,
                                () -> authenticationService.socialLogin("google", "valid-token", "1.1.1.1", "agent"));

                assertEquals("ACCOUNT_NOT_ACTIVE", ex.getCode());
        }

        @Test
        void linkSocialAccount_whenUserBanned_thenThrowsException() {
                java.util.Map<String, Object> verifiedData = new java.util.HashMap<>();
                verifiedData.put("providerId", "google-id");
                verifiedData.put("email", "test@nox.com");
                verifiedData.put("fullName", "Full Name");
                verifiedData.put("rawProfile", new java.util.HashMap<>());

                when(socialAuthVerificationService.verifyToken(anyString(), anyString())).thenReturn(verifiedData);
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(setupUser));
                setupUser.setStatus(UserStatus.BANNED);

                DomainException ex = assertThrows(DomainException.class,
                                () -> authenticationService.linkSocialAccount("google", "valid-token", "password",
                                                "1.1.1.1", "agent"));

                assertEquals("ACCOUNT_NOT_ACTIVE", ex.getCode());
        }
}
