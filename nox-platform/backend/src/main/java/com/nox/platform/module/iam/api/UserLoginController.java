package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.AuthRequest;
import com.nox.platform.module.iam.api.request.RefreshTokenRequest;
import com.nox.platform.module.iam.api.request.SocialLoginRequest;
import com.nox.platform.module.iam.api.response.AuthResponse;
import com.nox.platform.module.iam.api.response.TokenResponse;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.SocialAuthenticationService;
import com.nox.platform.module.iam.service.UserSessionService;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserLoginController {

    private final AuthenticationService authenticationService;
    private final UserSessionService userSessionService;
    private final SocialAuthenticationService socialAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationService.AuthResult result = authenticationService.authenticate(request.email(),
                request.password(),
                ipAddress,
                userAgent);
        User user = result.user();

        if (result.mfaRequired()) {
            return ResponseEntity.ok(ApiResponse.ok(
                    new AuthResponse(null, null, null, null, true, result.mfaToken())));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                new AuthResponse(user.getId().toString(), user.getEmail(), result.token(),
                        result.refreshToken(), false, null)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationService.AuthResult result = userSessionService.refreshAccessToken(
                request.refreshToken(), ipAddress,
                userAgent);

        return ResponseEntity.ok(ApiResponse.ok(
                new TokenResponse(result.token(), result.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request,
            Principal principal) {
        userSessionService.logout(request.refreshToken(), principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<TokenResponse>> socialLogin(@Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationService.AuthResult result = socialAuthenticationService.socialLogin(
                request.provider(), request.token(),
                ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.ok(
                new TokenResponse(result.token(), result.refreshToken())));
    }
}
