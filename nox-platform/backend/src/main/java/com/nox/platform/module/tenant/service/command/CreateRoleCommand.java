package com.nox.platform.module.tenant.service.command;

import java.util.List;
import java.util.UUID;

public record CreateRoleCommand(
    UUID orgId,
    String name,
    List<String> permissions,
    int level
) {}
