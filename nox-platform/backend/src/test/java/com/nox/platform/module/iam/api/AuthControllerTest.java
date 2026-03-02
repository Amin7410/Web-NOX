package com.nox.platform.module.iam.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.PasswordRecoveryService;
import com.nox.platform.module.iam.service.UserRegistrationService;
import com.nox.platform.module.iam.service.SocialAuthenticationService;
import com.nox.platform.module.iam.service.UserSessionService;
import com.nox.platform.module.iam.service.MfaManagementService;
import com.nox.platform.module.iam.service.MfaVerificationService;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.infra.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nox.platform.module.iam.api.request.*;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthenticationService authenticationService;

        @Mock
        private UserRegistrationService userRegistrationService;

        @Mock
        private PasswordRecoveryService passwordRecoveryService;

        @Mock
        private SocialAuthenticationService socialAuthenticationService;

        @Mock
        private UserSessionService userSessionService;

        @Mock
        private MfaManagementService mfaManagementService;

        @Mock
        private MfaVerificationService mfaVerificationService;

        @InjectMocks
        private AuthController authController;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void register_withValidPayload_returns200AndApiSuccess() throws Exception {
                UUID mockId = UUID.randomUUID();
                User mockUser = User.builder()
                                .id(mockId)
                                .email("test@test.com")
                                .fullName("Test User")
                                .status(UserStatus.PENDING_VERIFICATION)
                                .build();

                when(userRegistrationService.registerUser("test@test.com", "secure123", "Test User"))
                                .thenReturn(mockUser);

                RegisterRequest request = new RegisterRequest("test@test.com", "secure123", "Test User");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(mockId.toString()))
                                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                                .andExpect(jsonPath("$.data.status").value("PENDING_VERIFICATION"));
        }

        @Test
        void register_withInvalidEmail_returns400ValidationException() throws Exception {
                RegisterRequest request = new RegisterRequest("invalid-email", "secure123", null);
                // missing full name

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void register_withDuplicateEmail_returns400DomainException() throws Exception {
                when(userRegistrationService.registerUser(anyString(), anyString(), anyString()))
                                .thenThrow(new DomainException("EMAIL_ALREADY_EXISTS", "Email is taken", 400));

                RegisterRequest request = new RegisterRequest("duplicate@test.com", "secure123", "Dupe");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
                                .andExpect(jsonPath("$.error.message").value("Email is taken"));
        }

        @Test
        void refreshToken_withValidToken_returnsNewToken() throws Exception {
                when(userSessionService.refreshAccessToken(eq("valid-refresh-token"), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(null, "new-jwt-token",
                                                "valid-refresh-token",
                                                false, null));

                RefreshTokenRequest request = new RefreshTokenRequest(
                                "valid-refresh-token");

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").value("new-jwt-token"))
                                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"));
        }

        @Test
        void refreshToken_withInvalidToken_returns401() throws Exception {
                when(userSessionService.refreshAccessToken(eq("invalid-token"), any(), any()))
                                .thenThrow(new DomainException("INVALID_REFRESH_TOKEN",
                                                "Refresh token is invalid or expired", 401));

                RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
        }

        @Test
        void logout_returns200() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest(
                                "valid-refresh-token");

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void verifyEmail_returns200() throws Exception {
                VerifyEmailRequest request = new VerifyEmailRequest("test@nox.com",
                                "123456");

                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void setupMfa_returnsSecretAndUri() throws Exception {
                when(mfaManagementService.setupMfa(any()))
                                .thenReturn(new MfaManagementService.MfaSetupResult("secret123", "uri123"));

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/mfa/setup")
                                .principal(mockPrincipal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.secret").value("secret123"))
                                .andExpect(jsonPath("$.data.qrCodeUri").value("uri123"));
        }

        @Test
        void changePassword_returns200() throws Exception {
                ChangePasswordRequest request = new ChangePasswordRequest("old123",
                                "new123");

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void socialLogin_returnsTokens() throws Exception {
                when(socialAuthenticationService.socialLogin(eq("google"), eq("mock_token"), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(null, "jwt_token_123",
                                                "refresh_token_123",
                                                false, null));

                SocialLoginRequest request = new SocialLoginRequest("google", "mock_token");

                mockMvc.perform(post("/api/v1/auth/social-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").value("jwt_token_123"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh_token_123"));
        }
}
