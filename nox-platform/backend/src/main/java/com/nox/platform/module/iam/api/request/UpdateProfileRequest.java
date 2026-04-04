package com.nox.platform.module.iam.api.request;

public record UpdateProfileRequest(
        String fullName,
        String avatarUrl
) {
}
