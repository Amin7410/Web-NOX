package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Email is required") @Email String email,
        @NotBlank(message = "OTP Code is required") String otpCode,
        @NotBlank(message = "New Password is required") String newPassword) {
    @Override
    public String toString() {
        return "ResetPasswordRequest[email=" + email + ", otpCode=***, newPassword=***]";
    }
}
