package com.nox.platform.module.engine.api.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record CreateCoreBlockRequest(
        UUID parentBlockId,
        UUID originAssetId,
        @NotBlank(message = "Type is required") String type,
        @NotBlank(message = "Name is required") String name,
        Map<String, Object> config,
        Map<String, Object> visual) {
}
