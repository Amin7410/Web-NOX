package com.nox.platform.module.warehouse.service.command;

import java.util.UUID;

public record CreateCollectionCommand(
    UUID warehouseId,
    String name,
    UUID parentCollectionId
) {}
