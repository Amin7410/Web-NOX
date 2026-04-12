package com.nox.platform.module.warehouse.api.dto;

import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import java.util.Map;
import java.util.UUID;

public record InvaderDefinitionResponse(
    UUID id,
    UUID warehouseId,
    UUID collectionId,
    String code,
    String name,
    String category,
    Map<String, Object> configSchema,
    Map<String, Object> compilerHooks,
    String version
) {
    public static InvaderDefinitionResponse fromEntity(InvaderDefinition definition) {
        return new InvaderDefinitionResponse(
            definition.getId(),
            definition.getWarehouse().getId(),
            definition.getCollection() != null ? definition.getCollection().getId() : null,
            definition.getCode(),
            definition.getName(),
            definition.getCategory(),
            definition.getConfigSchema(),
            definition.getCompilerHooks(),
            definition.getTemplateVersion()
        );
    }
}
