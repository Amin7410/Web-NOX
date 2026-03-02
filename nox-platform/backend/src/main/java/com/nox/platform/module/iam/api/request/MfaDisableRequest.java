package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotBlank;

public record MfaDisableRequest(
        @NotBlank(message = "Current password is required to disable MFA") String password) {
}
