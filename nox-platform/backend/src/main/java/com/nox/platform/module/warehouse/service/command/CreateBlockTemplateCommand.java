package com.nox.platform.module.warehouse.service.command;

import java.util.Map;
import java.util.UUID;

public record CreateBlockTemplateCommand(
    UUID warehouseId,
    UUID collectionId,
    String name,
    String description,
    String thumbnailUrl,
    Map<String, Object> structureData,
    String version
) {}
