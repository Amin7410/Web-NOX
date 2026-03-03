package com.nox.platform.module.engine.api.response;

import com.nox.platform.module.engine.domain.WorkspaceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceResponse(
                UUID id,
                UUID projectId,
                String name,
                WorkspaceType type,
                UUID createdBy,
                OffsetDateTime createdAt) {
}
