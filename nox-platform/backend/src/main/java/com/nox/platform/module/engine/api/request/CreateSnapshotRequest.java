package com.nox.platform.module.engine.api.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSnapshotRequest(
                @NotBlank(message = "Snapshot name is required") @Size(max = 255, message = "Name must not exceed 255 characters") String name,

                String commitMessage,

                @NotNull(message = "Snapshot payload cannot be empty") JsonNode fullStateDump) {
}
