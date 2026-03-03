package com.nox.platform.module.engine.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SnapshotResponse(
                UUID id,
                UUID projectId,
                String name,
                String commitMessage,
                UUID createdById,
                OffsetDateTime createdAt) {
}
