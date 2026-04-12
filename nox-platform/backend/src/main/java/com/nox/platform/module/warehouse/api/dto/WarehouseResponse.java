package com.nox.platform.module.warehouse.api.dto;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import java.util.UUID;

public record WarehouseResponse(
    UUID id,
    UUID ownerId,
    OwnerType ownerType,
    String name,
    boolean isSystem
) {
    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        return new WarehouseResponse(
            warehouse.getId(),
            warehouse.getOwnerId(),
            warehouse.getOwnerType(),
            warehouse.getName(),
            warehouse.isSystem()
        );
    }
}
