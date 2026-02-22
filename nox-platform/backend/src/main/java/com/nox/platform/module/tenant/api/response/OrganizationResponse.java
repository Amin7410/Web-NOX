package com.nox.platform.module.tenant.api.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        Map<String, Object> settings,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
