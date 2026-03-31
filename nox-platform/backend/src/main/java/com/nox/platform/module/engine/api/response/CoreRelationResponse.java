package com.nox.platform.module.engine.api.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record CoreRelationResponse(
        UUID id,
        UUID workspaceId,
        UUID sourceBlockId,
        UUID targetBlockId,
        String type,
        Map<String, Object> rules,
        Map<String, Object> visual,
        OffsetDateTime deletedAt) {
}
