package com.nox.platform.module.engine.api.request;

import com.nox.platform.module.engine.domain.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required") @Size(max = 255, message = "Project name must not exceed 255 characters") String name,

        String description,

        ProjectVisibility visibility,
        
        @NotNull(message = "Organization ID is required") UUID organizationId) {
}
