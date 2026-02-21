package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
                @NotBlank(message = "Provider is required") String provider,
                @NotBlank(message = "Token is required") String token) {
}
