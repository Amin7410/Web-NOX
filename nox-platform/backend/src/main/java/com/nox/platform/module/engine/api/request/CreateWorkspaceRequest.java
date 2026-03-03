package com.nox.platform.module.engine.api.request;

import com.nox.platform.module.engine.domain.WorkspaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(
        @NotBlank(message = "Workspace name is required") @Size(max = 255, message = "Workspace name must not exceed 255 characters") String name,

        @NotNull(message = "Workspace type is required") WorkspaceType type) {
}
