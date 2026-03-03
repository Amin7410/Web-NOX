package com.nox.platform.module.engine.api.request;

import com.nox.platform.module.engine.domain.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required") @Size(max = 255, message = "Project name must not exceed 255 characters") String name,

        String description,

        ProjectVisibility visibility) {
}
