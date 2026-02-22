package com.nox.platform.module.tenant.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required") @Size(max = 100, message = "Organization name must be less than 100 characters") String name) {
}
