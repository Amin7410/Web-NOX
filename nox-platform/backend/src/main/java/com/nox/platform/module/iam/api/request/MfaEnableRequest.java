package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotNull;

public record MfaEnableRequest(
                @NotNull(message = "Code is required") Integer code) {
}
