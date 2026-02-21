package com.nox.platform.module.iam.api.response;

public record RegisterResponse(
        String id,
        String email,
        String status) {
}
