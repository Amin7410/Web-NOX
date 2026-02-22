package com.nox.platform.module.tenant.api.request;

import jakarta.validation.constraints.Size;
import java.util.Map;

public record UpdateOrganizationRequest(
        @Size(max = 100, message = "Organization name must be less than 100 characters") String name,

        Map<String, Object> settings) {
}
