package com.nox.platform.module.warehouse.service.command;

import java.util.Map;
import java.util.UUID;

public record CreateInvaderDefinitionCommand(
    UUID warehouseId,
    UUID collectionId,
    String code,
    String name,
    String category,
    Map<String, Object> configSchema,
    Map<String, Object> compilerHooks,
    String version
) {}
