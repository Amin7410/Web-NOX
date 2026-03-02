package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetTemplateService {

    private final BlockTemplateRepository blockTemplateRepository;
    private final InvaderDefinitionRepository invaderDefinitionRepository;
    private final WarehouseService warehouseService;
    private final AssetCollectionService collectionService;

    @Transactional
    public BlockTemplate createBlockTemplate(UUID warehouseId, UUID collectionId, String name, String description,
            String thumbnailUrl, Map<String, Object> structureData, String version) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());

        AssetCollection collection = null;
        if (collectionId != null) {
            collection = collectionService.getCollection(warehouseId, collectionId);
            if (!collection.getWarehouse().getId().equals(warehouseId)) {
                throw new DomainException("INVALID_COLLECTION", "Collection belongs to a different warehouse", 400);
            }
        }

        BlockTemplate template = BlockTemplate.builder()
                .warehouse(warehouse)
                .collection(collection)
                .name(name)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .structureData(structureData)
                .version(version)
                .build();

        return blockTemplateRepository.save(template);
    }

    public List<BlockTemplate> getBlockTemplatesByWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateReadOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
        return blockTemplateRepository.findByWarehouseId(warehouseId);
    }

    @Transactional
    public BlockTemplate updateBlockTemplate(UUID warehouseId, UUID id, String name, String description,
            String thumbnailUrl,
            Map<String, Object> structureData, String version) {
        BlockTemplate template = blockTemplateRepository.findById(id)
                .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND", "Block template not found", 404));

        if (!template.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Template does not belong to the specified warehouse", 400);
        }

        warehouseService.validateWriteOwnership(template.getWarehouse().getOwnerId(),
                template.getWarehouse().getOwnerType());

        if (name != null)
            template.setName(name);
        if (description != null)
            template.setDescription(description);
        if (thumbnailUrl != null)
            template.setThumbnailUrl(thumbnailUrl);
        if (structureData != null)
            template.setStructureData(structureData);
        if (version != null)
            template.setVersion(version);

        return blockTemplateRepository.save(template);
    }

    @Transactional
    public void deleteBlockTemplate(UUID warehouseId, UUID id) {
        BlockTemplate template = blockTemplateRepository.findById(id)
                .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND", "Block template not found", 404));

        if (!template.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Template does not belong to the specified warehouse", 400);
        }

        warehouseService.validateWriteOwnership(template.getWarehouse().getOwnerId(),
                template.getWarehouse().getOwnerType());

        template.softDelete();
        blockTemplateRepository.save(template);
    }

    @Transactional
    public InvaderDefinition createInvaderDefinition(UUID warehouseId, UUID collectionId, String code, String name,
            String category, Map<String, Object> configSchema, Map<String, Object> compilerHooks, String version) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());

        if (invaderDefinitionRepository.findByWarehouseIdAndCode(warehouseId, code).isPresent()) {
            throw new DomainException("INVADER_CODE_EXISTS", "Invader code already exists in this warehouse", 400);
        }

        AssetCollection collection = null;
        if (collectionId != null) {
            collection = collectionService.getCollection(warehouseId, collectionId);
            if (!collection.getWarehouse().getId().equals(warehouseId)) {
                throw new DomainException("INVALID_COLLECTION", "Collection belongs to a different warehouse", 400);
            }
        }

        InvaderDefinition definition = InvaderDefinition.builder()
                .warehouse(warehouse)
                .collection(collection)
                .code(code)
                .name(name)
                .category(category)
                .configSchema(configSchema)
                .compilerHooks(compilerHooks)
                .version(version)
                .build();

        return invaderDefinitionRepository.save(definition);
    }

    public List<InvaderDefinition> getInvaderDefinitionsByWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateReadOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
        return invaderDefinitionRepository.findByWarehouseId(warehouseId);
    }

    @Transactional
    public InvaderDefinition updateInvaderDefinition(UUID warehouseId, UUID id, String name, String category,
            Map<String, Object> configSchema, Map<String, Object> compilerHooks, String version) {
        InvaderDefinition definition = invaderDefinitionRepository.findById(id)
                .orElseThrow(() -> new DomainException("INVADER_NOT_FOUND", "Invader definition not found", 404));

        if (!definition.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE",
                    "Invader definition does not belong to the specified warehouse", 400);
        }

        warehouseService.validateWriteOwnership(definition.getWarehouse().getOwnerId(),
                definition.getWarehouse().getOwnerType());

        if (name != null)
            definition.setName(name);
        if (category != null)
            definition.setCategory(category);
        if (configSchema != null)
            definition.setConfigSchema(configSchema);
        if (compilerHooks != null)
            definition.setCompilerHooks(compilerHooks);
        if (version != null)
            definition.setVersion(version);

        return invaderDefinitionRepository.save(definition);
    }

    @Transactional
    public void deleteInvaderDefinition(UUID warehouseId, UUID id) {
        InvaderDefinition definition = invaderDefinitionRepository.findById(id)
                .orElseThrow(() -> new DomainException("INVADER_NOT_FOUND", "Invader definition not found", 404));

        if (!definition.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE",
                    "Invader definition does not belong to the specified warehouse", 400);
        }

        warehouseService.validateWriteOwnership(definition.getWarehouse().getOwnerId(),
                definition.getWarehouse().getOwnerType());

        definition.softDelete();
        invaderDefinitionRepository.save(definition);
    }
}
