package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.MfaDisableRequest;
import com.nox.platform.module.iam.api.request.MfaEnableRequest;
import com.nox.platform.module.iam.api.request.MfaVerifyRequest;
import com.nox.platform.module.iam.api.request.VerifyMfaBackupCodeRequest;
import com.nox.platform.module.iam.api.response.AuthResponse;
import com.nox.platform.module.iam.api.response.MfaEnableResponse;
import com.nox.platform.module.iam.api.response.MfaSetupResponse;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.AuthenticationService;
import com.nox.platform.module.iam.service.MfaManagementService;
import com.nox.platform.module.iam.service.MfaVerificationService;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/mfa")
@RequiredArgsConstructor
public class UserMfaController {

    private final MfaManagementService mfaManagementService;
    private final MfaVerificationService mfaVerificationService;

    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(Principal principal) {
        MfaManagementService.MfaSetupResult result = mfaManagementService.setupMfa(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(
                new MfaSetupResponse(result.secret(), result.qrCodeUri())));
    }

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<MfaEnableResponse>> enableMfa(
            @Valid @RequestBody MfaEnableRequest request, Principal principal) {
        List<String> backupCodes = mfaManagementService.enableMfa(principal.getName(), request.code());
        return ResponseEntity.ok(ApiResponse.ok(
                new MfaEnableResponse(backupCodes)));
    }

    @PostMapping("/verify")
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

    @PostMapping("/verify-backup")
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

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> disableMfa(
            @Valid @RequestBody MfaDisableRequest request, Principal principal) {
        mfaManagementService.disableMfa(principal.getName(), request.password());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
