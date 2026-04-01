package com.nox.platform.module.engine.api.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record BlockInvaderUsageResponse(
        UUID id,
        UUID blockId,
        UUID invaderAssetId,
        String appliedVersion,
        Map<String, Object> configSnapshot,
        OffsetDateTime createdAt) {
}
