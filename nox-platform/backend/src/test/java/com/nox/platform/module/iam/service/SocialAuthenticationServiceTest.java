package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialAuthenticationServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private SocialIdentityRepository socialIdentityRepository;

        @Mock
        private SocialAuthVerificationService socialAuthVerificationService;

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtService jwtService;

        @Mock
        private UserSessionService userSessionService;

        @InjectMocks
        private SocialAuthenticationService socialAuthenticationService;

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
                                () -> socialAuthenticationService.socialLogin("google", "valid-token", "1.1.1.1",
                                                "agent"));

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
                                () -> socialAuthenticationService.linkSocialAccount("google", "valid-token", "password",
                                                "1.1.1.1", "agent"));

                assertEquals("ACCOUNT_NOT_ACTIVE", ex.getCode());
        }
}
