package com.nox.platform.module.engine.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record CreateCoreRelationRequest(
        @NotNull(message = "Source block ID is required") UUID sourceBlockId,
        @NotNull(message = "Target block ID is required") UUID targetBlockId,
        @NotBlank(message = "Type is required") String type,
        Map<String, Object> rules,
        Map<String, Object> visual) {
}
