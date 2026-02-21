package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Email is required") @Email String email,
        @NotBlank(message = "OTP Code is required") String otpCode) {
}
