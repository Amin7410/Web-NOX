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

        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "token", result.token(),
                        "refreshToken", result.refreshToken())));
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

    public record RefreshTokenRequest(
            @jakarta.validation.constraints.NotBlank(message = "Refresh token is required") String refreshToken) {
    }
}
