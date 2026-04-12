package com.nox.platform.module.warehouse.service.command;

import java.util.Map;

public record UpdateInvaderDefinitionCommand(
    String name,
    String category,
    Map<String, Object> configSchema,
    Map<String, Object> compilerHooks,
    String version
) {}
