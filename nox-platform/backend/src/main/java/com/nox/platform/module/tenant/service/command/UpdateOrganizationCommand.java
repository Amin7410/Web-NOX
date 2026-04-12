package com.nox.platform.module.tenant.service.command;

import java.util.Map;
import java.util.UUID;

public record UpdateOrganizationCommand(
    UUID orgId,
    String name,
    Map<String, Object> settings
) {}
