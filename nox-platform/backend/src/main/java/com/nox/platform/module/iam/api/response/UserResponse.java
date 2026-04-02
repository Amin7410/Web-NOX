package com.nox.platform.module.iam.api.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String status
) {
}
