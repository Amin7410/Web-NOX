package com.nox.platform.module.tenant.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRoleRequest(
                @NotBlank(message = "Role name is required") @Size(max = 50, message = "Role name must be less than 50 characters") String name,

                @NotEmpty(message = "At least one permission is required") List<String> permissions,

                Integer level) {
}
