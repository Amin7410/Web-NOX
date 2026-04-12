package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.ChangePasswordRequest;
import com.nox.platform.module.iam.api.request.ForgotPasswordRequest;
import com.nox.platform.module.iam.api.request.ResetPasswordRequest;
import com.nox.platform.module.iam.service.PasswordRecoveryService;
import com.nox.platform.shared.api.ApiResponse;
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
public class UserPasswordController {

    private final PasswordRecoveryService passwordRecoveryService;

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

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        passwordRecoveryService.changePassword(principal.getName(), request.oldPassword(),
                request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
