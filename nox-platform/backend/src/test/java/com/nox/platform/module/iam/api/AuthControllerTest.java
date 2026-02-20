package com.nox.platform.module.iam.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.service.AuthService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthService authService;

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

                when(authService.registerUser("test@test.com", "secure123", "Test User")).thenReturn(mockUser);

                RegisterRequest request = new RegisterRequest();
                request.setEmail("test@test.com");
                request.setPassword("secure123");
                request.setFullName("Test User");

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
                RegisterRequest request = new RegisterRequest();
                request.setEmail("invalid-email");
                request.setPassword("secure123");
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
                when(authService.registerUser(anyString(), anyString(), anyString()))
                                .thenThrow(new DomainException("EMAIL_ALREADY_EXISTS", "Email is taken", 400));

                RegisterRequest request = new RegisterRequest();
                request.setEmail("duplicate@test.com");
                request.setPassword("secure123");
                request.setFullName("Dupe");

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
                when(authService.refreshAccessToken("valid-refresh-token"))
                                .thenReturn(new AuthService.AuthResult(null, "new-jwt-token", "valid-refresh-token"));

                AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest(
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
                when(authService.refreshAccessToken("invalid-token"))
                                .thenThrow(new DomainException("INVALID_REFRESH_TOKEN",
                                                "Refresh token is invalid or expired", 401));

                AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest("invalid-token");

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
        }
}
