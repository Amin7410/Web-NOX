package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.AuthService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerUser(request.getEmail(), request.getPassword(), request.getFullName());

        // Don't leak the whole user entity out directly. In a real scenario, use a
        // Response DTO.
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "status", user.getStatus().name())));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.authenticate(request.getEmail(), request.getPassword(), ipAddress,
                userAgent);
        User user = result.user();

        if (result.mfaRequired()) {
            return ResponseEntity.ok(ApiResponse.ok(
                    Map.of(
                            "mfaRequired", "true",
                            "mfaToken", result.mfaToken())));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "token", result.token(),
                        "refreshToken", result.refreshToken(),
                        "mfaRequired", "false")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.refreshAccessToken(request.refreshToken(), ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "token", result.token(),
                        "refreshToken", result.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.code());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.code(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<ApiResponse<Map<String, String>>> setupMfa(@Valid @RequestBody MfaSetupRequest request) {
        AuthService.MfaSetupResult result = authService.setupMfa(request.email());
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "secret", result.secret(),
                        "qrCodeUri", result.qrCodeUri())));
    }

    @PostMapping("/mfa/enable")
    public ResponseEntity<ApiResponse<Void>> enableMfa(@Valid @RequestBody MfaEnableRequest request) {
        authService.enableMfa(request.email(), request.secret(), request.code());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.verifyMfa(request.mfaToken(), request.code(), ipAddress, userAgent);
        User user = result.user();

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "token", result.token(),
                        "refreshToken", result.refreshToken())));
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<Map<String, String>>> socialLogin(@Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.AuthResult result = authService.socialLogin(
                request.provider(), request.providerId(), request.email(), request.fullName(), request.profileData(),
                ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "token", result.token(),
                "refreshToken", result.refreshToken())));
    }

    // --- Auxiliary payload records ---

    public record RefreshTokenRequest(
            @jakarta.validation.constraints.NotBlank(message = "Refresh token is required") String refreshToken) {
    }

    public record VerifyEmailRequest(
            @jakarta.validation.constraints.NotBlank(message = "OTP Code is required") String code) {
    }

    public record ForgotPasswordRequest(
            @jakarta.validation.constraints.NotBlank(message = "Email is required") @jakarta.validation.constraints.Email(message = "Email should be valid") String email) {
    }

    public record ResetPasswordRequest(
            @jakarta.validation.constraints.NotBlank(message = "OTP Code is required") String code,
            @jakarta.validation.constraints.NotBlank(message = "New Password is required") String newPassword) {
    }

    public record MfaSetupRequest(
            @jakarta.validation.constraints.NotBlank(message = "Email is required") @jakarta.validation.constraints.Email String email) {
    }

    public record MfaEnableRequest(
            @jakarta.validation.constraints.NotBlank(message = "Email is required") @jakarta.validation.constraints.Email String email,
            @jakarta.validation.constraints.NotBlank(message = "Secret is required") String secret,
            @jakarta.validation.constraints.NotNull(message = "Code is required") Integer code) {
    }

    public record MfaVerifyRequest(
            @jakarta.validation.constraints.NotBlank(message = "MFA Token is required") String mfaToken,
            @jakarta.validation.constraints.NotNull(message = "Code is required") Integer code) {
    }

    public record SocialLoginRequest(
            @jakarta.validation.constraints.NotBlank(message = "Provider is required") String provider,
            @jakarta.validation.constraints.NotBlank(message = "Provider ID is required") String providerId,
            @jakarta.validation.constraints.NotBlank(message = "Email is required") @jakarta.validation.constraints.Email(message = "Email should be valid") String email,
            String fullName,
            Map<String, Object> profileData) {
    }
}
