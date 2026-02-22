package com.nox.platform.module.tenant.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddMemberRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Role name is required") @Size(max = 50, message = "Role name must be less than 50 characters") String roleName) {
}
