package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.*;
import com.nox.platform.module.iam.api.response.*;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.SocialAuthenticationService;
import com.nox.platform.module.iam.service.UserSessionService;
import com.nox.platform.module.iam.service.MfaManagementService;
import com.nox.platform.module.iam.service.MfaVerificationService;
import com.nox.platform.module.iam.service.PasswordRecoveryService;
import com.nox.platform.module.iam.service.UserRegistrationService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationService authenticationService;
        private final UserRegistrationService userRegistrationService;
        private final PasswordRecoveryService passwordRecoveryService;
        private final SocialAuthenticationService socialAuthenticationService;
        private final UserSessionService userSessionService;
        private final MfaManagementService mfaManagementService;
        private final MfaVerificationService mfaVerificationService;

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
                User user = userRegistrationService.registerUser(request.email(), request.password(),
                                request.fullName());

                return ResponseEntity.ok(ApiResponse.ok(
                                new RegisterResponse(user.getId().toString(), user.getEmail(),
                                                user.getStatus().name())));
        }

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

        @PostMapping("/verify-email")
        public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
                userRegistrationService.verifyEmail(request.email(), request.otpCode());
                return ResponseEntity.ok(ApiResponse.ok(null));
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                passwordRecoveryService.forgotPassword(request.email());
                return ResponseEntity.ok(ApiResponse.ok(null));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                passwordRecoveryService.resetPassword(request.email(), request.otpCode(), request.newPassword());
                return ResponseEntity.ok(ApiResponse.ok(null));
        }

        @PostMapping("/mfa/setup")
        public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(Principal principal) {
                MfaManagementService.MfaSetupResult result = mfaManagementService.setupMfa(principal.getName());
                return ResponseEntity.ok(ApiResponse.ok(
                                new MfaSetupResponse(result.secret(), result.qrCodeUri())));
        }

        @PostMapping("/mfa/enable")
        public ResponseEntity<ApiResponse<MfaEnableResponse>> enableMfa(
                        @Valid @RequestBody MfaEnableRequest request, Principal principal) {
                List<String> backupCodes = mfaManagementService.enableMfa(principal.getName(), request.code());
                return ResponseEntity.ok(ApiResponse.ok(
                                new MfaEnableResponse(backupCodes)));
        }

        @PostMapping("/mfa/verify")
        public ResponseEntity<ApiResponse<AuthResponse>> verifyMfa(
                        @Valid @RequestBody MfaVerifyRequest request, HttpServletRequest httpRequest) {
                String ipAddress = IpUtils.getClientIp(httpRequest);
                String userAgent = httpRequest.getHeader("User-Agent");

                AuthenticationService.AuthResult result = mfaVerificationService.verifyMfa(request.mfaToken(),
                                request.code(), ipAddress,
                                userAgent);
                User user = result.user();

                return ResponseEntity.ok(ApiResponse.ok(
                                new AuthResponse(user.getId().toString(), user.getEmail(), result.token(),
                                                result.refreshToken(), null, null)));
        }

        @PostMapping("/mfa/verify-backup")
        public ResponseEntity<ApiResponse<AuthResponse>> verifyMfaBackupCode(
                        @Valid @RequestBody VerifyMfaBackupCodeRequest request, HttpServletRequest httpRequest) {
                String ipAddress = IpUtils.getClientIp(httpRequest);
                String userAgent = httpRequest.getHeader("User-Agent");

                AuthenticationService.AuthResult result = mfaVerificationService.verifyMfaBackupCode(
                                request.mfaToken(),
                                request.backupCode(),
                                ipAddress, userAgent);
                User user = result.user();

                return ResponseEntity.ok(ApiResponse.ok(
                                new AuthResponse(user.getId().toString(), user.getEmail(), result.token(),
                                                result.refreshToken(), null, null)));
        }

        @org.springframework.web.bind.annotation.DeleteMapping("/mfa")
        public ResponseEntity<ApiResponse<Void>> disableMfa(
                        @Valid @RequestBody MfaDisableRequest request, Principal principal) {
                mfaManagementService.disableMfa(principal.getName(), request.password());
                return ResponseEntity.ok(ApiResponse.ok(null));
        }

        @PostMapping("/change-password")
        public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                        Principal principal) {
                passwordRecoveryService.changePassword(principal.getName(), request.oldPassword(),
                                request.newPassword());
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
