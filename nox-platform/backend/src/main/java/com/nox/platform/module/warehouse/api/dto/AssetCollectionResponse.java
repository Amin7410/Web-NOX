package com.nox.platform.module.warehouse.api.dto;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import java.util.UUID;

public record AssetCollectionResponse(
    UUID id,
    UUID warehouseId,
    String name,
    UUID parentId
) {
    public static AssetCollectionResponse fromEntity(AssetCollection collection) {
        return new AssetCollectionResponse(
            collection.getId(),
            collection.getWarehouse().getId(),
            collection.getName(),
            collection.getParentCollection() != null ? collection.getParentCollection().getId() : null
        );
    }
}
