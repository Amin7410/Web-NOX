package com.nox.platform.module.iam.api.response;

public record TokenResponse(
        String token,
        String refreshToken) {
}
