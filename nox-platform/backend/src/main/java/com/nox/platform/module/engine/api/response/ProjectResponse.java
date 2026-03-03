package com.nox.platform.module.engine.api.response;

import com.nox.platform.module.engine.domain.ProjectStatus;
import com.nox.platform.module.engine.domain.ProjectVisibility;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
                UUID id,
                String name,
                String slug,
                String description,
                ProjectVisibility visibility,
                ProjectStatus status,
                UUID createdById,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt) {
}
