package com.nox.platform.module.engine.api.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record CoreBlockResponse(
        UUID id,
        UUID workspaceId,
        UUID parentBlockId,
        UUID originAssetId,
        String type,
        String name,
        Map<String, Object> config,
        Map<String, Object> visual,
        UUID createdById,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt) {
}
