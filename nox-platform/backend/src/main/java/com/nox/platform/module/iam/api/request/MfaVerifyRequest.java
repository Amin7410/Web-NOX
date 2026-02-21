package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MfaVerifyRequest(
        @NotBlank(message = "MFA Token is required") String mfaToken,
        @NotNull(message = "Code is required") Integer code) {
}
