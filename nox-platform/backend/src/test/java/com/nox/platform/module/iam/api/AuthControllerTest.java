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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        void register_withWeakPassword_returns400() throws Exception {
                RegisterRequest request = new RegisterRequest("test@test.com", "123", "Test User");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void register_withMissingFields_returns400() throws Exception {
                RegisterRequest request = new RegisterRequest(null, null, null);

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void login_withValidCredentials_returns200() throws Exception {
                UUID mockUserId = UUID.randomUUID();
                User mockUser = User.builder()
                                .id(mockUserId)
                                .email("test@nox.com")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(authenticationService.authenticate(eq("test@nox.com"), eq("password123"), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(mockUser, "jwt-token-123",
                                                "refresh-token-123", false, null));

                AuthRequest request = new AuthRequest("test@nox.com", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(mockUserId.toString()))
                                .andExpect(jsonPath("$.data.email").value("test@nox.com"))
                                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-123"))
                                .andExpect(jsonPath("$.data.mfaRequired").value(false));
        }

        @Test
        void login_withMfaRequired_returns200WithMfaToken() throws Exception {
                when(authenticationService.authenticate(eq("test@nox.com"), eq("password123"), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(null, null, null, true, "mfa-token-123"));

                AuthRequest request = new AuthRequest("test@nox.com", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.mfaRequired").value(true))
                                .andExpect(jsonPath("$.data.mfaToken").value("mfa-token-123"));
        }

        @Test
        void login_withInvalidCredentials_returns401() throws Exception {
                when(authenticationService.authenticate(eq("test@nox.com"), eq("wrongpassword"), any(), any()))
                                .thenThrow(new DomainException("INVALID_CREDENTIALS", "Email or password is incorrect", 401));

                AuthRequest request = new AuthRequest("test@nox.com", "wrongpassword");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        void login_withMissingFields_returns400() throws Exception {
                AuthRequest request = new AuthRequest(null, null);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
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
        void refreshToken_withMissingToken_returns400() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest(null);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
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
        void logout_withInvalidToken_returns400() throws Exception {
                doThrow(new DomainException("INVALID_REFRESH_TOKEN", "Refresh token is invalid", 400))
                                .when(userSessionService).logout(anyString(), anyString());

                RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
        }

        @Test
        void verifyEmail_withValidData_returns200() throws Exception {
                VerifyEmailRequest request = new VerifyEmailRequest("test@nox.com", "123456");

                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void verifyEmail_withInvalidEmail_returns400() throws Exception {
                VerifyEmailRequest request = new VerifyEmailRequest("invalid-email", "123456");

                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void verifyEmail_withInvalidOtp_returns400() throws Exception {
                doThrow(new DomainException("INVALID_OTP", "OTP is invalid or expired", 400))
                                .when(userRegistrationService).verifyEmail(anyString(), anyString());

                VerifyEmailRequest request = new VerifyEmailRequest("test@nox.com", "000000");

                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_OTP"));
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
        void setupMfa_whenMfaAlreadyEnabled_returns400() throws Exception {
                when(mfaManagementService.setupMfa(any()))
                                .thenThrow(new DomainException("MFA_ALREADY_ENABLED", "MFA is already enabled", 400));

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/mfa/setup")
                                .principal(mockPrincipal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("MFA_ALREADY_ENABLED"));
        }

        @Test
        void enableMfa_withValidCode_returns200() throws Exception {
                List<String> backupCodes = List.of("backup1", "backup2", "backup3");
                when(mfaManagementService.enableMfa(anyString(), anyInt()))
                                .thenReturn(backupCodes);

                MfaEnableRequest request = new MfaEnableRequest(123456);
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/mfa/enable")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.backupCodes").isArray())
                                .andExpect(jsonPath("$.data.backupCodes[0]").value("backup1"));
        }

        @Test
        void enableMfa_withInvalidCode_returns400() throws Exception {
                when(mfaManagementService.enableMfa(anyString(), anyInt()))
                                .thenThrow(new DomainException("INVALID_MFA_CODE", "MFA code is invalid", 400));

                MfaEnableRequest request = new MfaEnableRequest(000000);
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/mfa/enable")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_MFA_CODE"));
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
        void changePassword_withInvalidOldPassword_returns400() throws Exception {
                doThrow(new DomainException("INVALID_OLD_PASSWORD", "Old password is incorrect", 400))
                                .when(passwordRecoveryService).changePassword(anyString(), anyString(), anyString());

                ChangePasswordRequest request = new ChangePasswordRequest("wrongold", "new123");
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_OLD_PASSWORD"));
        }

        @Test
        void changePassword_withMissingFields_returns400() throws Exception {
                ChangePasswordRequest request = new ChangePasswordRequest(null, null);

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void forgotPassword_withValidEmail_returns200() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest("test@nox.com");

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void forgotPassword_withInvalidEmail_returns400() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email");

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void forgotPassword_withNonExistentEmail_returns200() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@nox.com");

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void forgotPassword_withMissingEmail_returns400() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest(null);

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void verifyMfa_withValidData_returns200() throws Exception {
                UUID mockUserId = UUID.randomUUID();
                User mockUser = User.builder()
                                .id(mockUserId)
                                .email("test@nox.com")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(mfaVerificationService.verifyMfa(eq("mfa-token-123"), eq(123456), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(mockUser, "jwt-token-123",
                                                "refresh-token-123", false, null));

                MfaVerifyRequest request = new MfaVerifyRequest("mfa-token-123", 123456);

                mockMvc.perform(post("/api/v1/auth/mfa/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(mockUserId.toString()))
                                .andExpect(jsonPath("$.data.email").value("test@nox.com"))
                                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-123"));
        }

        @Test
        void verifyMfa_withInvalidCode_returns401() throws Exception {
                when(mfaVerificationService.verifyMfa(eq("mfa-token-123"), eq(0), any(), any()))
                                .thenThrow(new DomainException("INVALID_MFA_CODE", "MFA code is invalid", 401));

                MfaVerifyRequest request = new MfaVerifyRequest("mfa-token-123", 0);

                mockMvc.perform(post("/api/v1/auth/mfa/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_MFA_CODE"));
        }

        @Test
        void verifyMfa_withMissingData_returns400() throws Exception {
                MfaVerifyRequest request = new MfaVerifyRequest(null, null);

                mockMvc.perform(post("/api/v1/auth/mfa/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void verifyMfaBackupCode_withValidData_returns200() throws Exception {
                UUID mockUserId = UUID.randomUUID();
                User mockUser = User.builder()
                                .id(mockUserId)
                                .email("test@nox.com")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(mfaVerificationService.verifyMfaBackupCode(eq("mfa-token-123"), eq("backup-code-123"), any(), any()))
                                .thenReturn(new AuthenticationService.AuthResult(mockUser, "jwt-token-123",
                                                "refresh-token-123", false, null));

                VerifyMfaBackupCodeRequest request = new VerifyMfaBackupCodeRequest("mfa-token-123", "backup-code-123");

                mockMvc.perform(post("/api/v1/auth/mfa/verify-backup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(mockUserId.toString()))
                                .andExpect(jsonPath("$.data.email").value("test@nox.com"))
                                .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-123"));
        }

        @Test
        void verifyMfaBackupCode_withInvalidCode_returns401() throws Exception {
                when(mfaVerificationService.verifyMfaBackupCode(eq("mfa-token-123"), eq("invalid-backup"), any(), any()))
                                .thenThrow(new DomainException("INVALID_BACKUP_CODE", "Backup code is invalid", 401));

                VerifyMfaBackupCodeRequest request = new VerifyMfaBackupCodeRequest("mfa-token-123", "invalid-backup");

                mockMvc.perform(post("/api/v1/auth/mfa/verify-backup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_BACKUP_CODE"));
        }

        @Test
        void verifyMfaBackupCode_withMissingData_returns400() throws Exception {
                VerifyMfaBackupCodeRequest request = new VerifyMfaBackupCodeRequest(null, null);

                mockMvc.perform(post("/api/v1/auth/mfa/verify-backup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
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

        
        @Test
        void socialLogin_withInvalidToken_returns401() throws Exception {
                when(socialAuthenticationService.socialLogin(eq("google"), eq("invalid_token"), any(), any()))
                                .thenThrow(new DomainException("INVALID_SOCIAL_TOKEN", "Social token is invalid", 401));

                SocialLoginRequest request = new SocialLoginRequest("google", "invalid_token");

                mockMvc.perform(post("/api/v1/auth/social-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_SOCIAL_TOKEN"));
        }

        @Test
        void disableMfa_withValidPassword_returns200() throws Exception {
                MfaDisableRequest request = new MfaDisableRequest("password123");
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(delete("/api/v1/auth/mfa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void disableMfa_withInvalidPassword_returns400() throws Exception {
                doThrow(new DomainException("INVALID_PASSWORD", "Password is incorrect", 400))
                                .when(mfaManagementService).disableMfa(anyString(), anyString());

                MfaDisableRequest request = new MfaDisableRequest("wrongpassword");
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(delete("/api/v1/auth/mfa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_PASSWORD"));
        }

        @Test
        void disableMfa_whenMfaNotEnabled_returns400() throws Exception {
                doThrow(new DomainException("MFA_NOT_ENABLED", "MFA is not enabled", 400))
                                .when(mfaManagementService).disableMfa(anyString(), anyString());

                MfaDisableRequest request = new MfaDisableRequest("password123");
                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(delete("/api/v1/auth/mfa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("MFA_NOT_ENABLED"));
        }

        @Test
        void disableMfa_withMissingPassword_returns400() throws Exception {
                MfaDisableRequest request = new MfaDisableRequest(null);

                java.security.Principal mockPrincipal = org.mockito.Mockito.mock(java.security.Principal.class);
                when(mockPrincipal.getName()).thenReturn("test@nox.com");

                mockMvc.perform(delete("/api/v1/auth/mfa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .principal(mockPrincipal)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void resetPassword_withValidData_returns200() throws Exception {
                ResetPasswordRequest request = new ResetPasswordRequest("test@nox.com", "123456", "newPassword123");

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void resetPassword_withInvalidEmail_returns400() throws Exception {
                ResetPasswordRequest request = new ResetPasswordRequest("invalid-email", "123456", "newPassword123");

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        void resetPassword_withInvalidOtp_returns400() throws Exception {
                doThrow(new DomainException("INVALID_OTP", "OTP is invalid or expired", 400))
                                .when(passwordRecoveryService).resetPassword(anyString(), anyString(), anyString());

                ResetPasswordRequest request = new ResetPasswordRequest("test@nox.com", "000000", "newPassword123");

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_OTP"));
        }
}
