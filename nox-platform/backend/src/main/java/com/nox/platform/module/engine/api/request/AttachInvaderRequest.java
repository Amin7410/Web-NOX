package com.nox.platform.module.engine.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record AttachInvaderRequest(
        @NotNull(message = "Invader asset ID is required") UUID invaderAssetId,
        String appliedVersion,
        Map<String, Object> configSnapshot) {
}
