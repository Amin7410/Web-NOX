package com.nox.platform.module.tenant.service.command;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationCommand(
    @NotBlank String name,
    @NotBlank String creatorEmail
) {}
