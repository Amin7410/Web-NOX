package com.nox.platform.module.warehouse.api.dto;

import com.nox.platform.module.warehouse.domain.BlockTemplate;
import java.util.Map;
import java.util.UUID;

public record BlockTemplateResponse(
    UUID id,
    UUID warehouseId,
    UUID collectionId,
    String name,
    String description,
    String thumbnailUrl,
    Map<String, Object> structureData,
    String version
) {
    public static BlockTemplateResponse fromEntity(BlockTemplate template) {
        return new BlockTemplateResponse(
            template.getId(),
            template.getWarehouse().getId(),
            template.getCollection() != null ? template.getCollection().getId() : null,
            template.getName(),
            template.getDescription(),
            template.getThumbnailUrl(),
            template.getStructureData(),
            template.getTemplateVersion()
        );
    }
}
