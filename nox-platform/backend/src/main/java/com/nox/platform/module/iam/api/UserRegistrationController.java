package com.nox.platform.module.iam.api;

import com.nox.platform.module.iam.api.request.RegisterRequest;
import com.nox.platform.module.iam.api.request.VerifyEmailRequest;
import com.nox.platform.module.iam.api.response.RegisterResponse;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.service.UserRegistrationService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userRegistrationService.registerUser(request.email(), request.password(), request.fullName());

        return ResponseEntity.ok(ApiResponse.ok(
                new RegisterResponse(user.getId().toString(), user.getEmail(), user.getStatus().name())));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userRegistrationService.verifyEmail(request.email(), request.otpCode());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
