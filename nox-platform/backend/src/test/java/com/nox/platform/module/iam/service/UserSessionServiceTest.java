package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.domain.UserSession;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

        @Mock
        private UserSessionRepository userSessionRepository;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private UserSessionService userSessionService;

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

                ReflectionTestUtils.setField(userSessionService, "refreshTokenExpirationDays", 7);
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

                AuthenticationService.AuthResult result = userSessionService.refreshAccessToken("valid-token",
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

                DomainException ex = assertThrows(DomainException.class, () -> userSessionService
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

                DomainException ex = assertThrows(DomainException.class, () -> userSessionService
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
                                () -> userSessionService.logout("valid-token", "attacker@nox.com"));

                assertEquals("UNAUTHORIZED_LOGOUT", ex.getCode());
        }
}
