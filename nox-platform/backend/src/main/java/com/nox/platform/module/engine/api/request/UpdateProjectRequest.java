package com.nox.platform.module.engine.api.request;

import com.nox.platform.module.engine.domain.ProjectStatus;
import com.nox.platform.module.engine.domain.ProjectVisibility;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 255, message = "Project name must not exceed 255 characters") String name,

        String description,

        ProjectVisibility visibility,

        ProjectStatus status) {
}
