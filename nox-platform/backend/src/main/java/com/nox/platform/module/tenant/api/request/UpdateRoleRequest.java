package com.nox.platform.module.tenant.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateRoleRequest(
        @NotNull(message = "Permissions list cannot be null") List<String> permissions) {
}
