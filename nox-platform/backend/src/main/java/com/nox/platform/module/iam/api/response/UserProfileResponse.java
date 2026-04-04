package com.nox.platform.module.iam.api.response;

public record UserProfileResponse(
        String id,
        String email,
        String fullName,
        String avatarUrl,
        boolean isEmailVerified
) {
}
