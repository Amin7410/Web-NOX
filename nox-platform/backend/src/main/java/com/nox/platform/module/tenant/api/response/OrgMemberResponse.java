package com.nox.platform.module.tenant.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrgMemberResponse(
        UUID id,
        UUID userId,
        String email,
        String fullName,
        RoleResponse role,
        OffsetDateTime joinedAt) {
}
