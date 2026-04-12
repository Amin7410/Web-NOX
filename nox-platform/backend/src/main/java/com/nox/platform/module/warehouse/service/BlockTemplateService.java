package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.command.CreateBlockTemplateCommand;
import com.nox.platform.module.warehouse.service.command.UpdateBlockTemplateCommand;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockTemplateService {

    private final BlockTemplateRepository blockTemplateRepository;
    private final WarehouseRepository warehouseRepository;
    private final AssetCollectionService collectionService;
    private final com.nox.platform.shared.abstraction.TimeProvider timeProvider;
    private final WarehouseAccessValidator accessValidator;

    @Transactional
    public BlockTemplate createBlockTemplate(CreateBlockTemplateCommand command) {
        Warehouse warehouse = getWarehouseAndValidateRead(command.warehouseId());
        accessValidator.validateWriteAccess(warehouse.getOwnerId(), warehouse.getOwnerType());

        AssetCollection collection = null;
        if (command.collectionId() != null) {
            collection = collectionService.getCollection(command.warehouseId(), command.collectionId());
        }

        BlockTemplate template = BlockTemplate.create(warehouse, collection, command, timeProvider.now());
        return blockTemplateRepository.save(template);
    }

    @Transactional
    public void softDeleteAllByWarehouse(UUID warehouseId, OffsetDateTime now) {
        blockTemplateRepository.softDeleteByWarehouseId(warehouseId, now);
    }

    public List<BlockTemplate> getBlockTemplatesByWarehouse(UUID warehouseId) {
        getWarehouseAndValidateRead(warehouseId);
        return blockTemplateRepository.findByWarehouseId(warehouseId);
    }

    @Transactional
    public BlockTemplate updateBlockTemplate(UUID warehouseId, UUID id, UpdateBlockTemplateCommand command) {
        BlockTemplate template = blockTemplateRepository.findById(id)
                .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND", "Block template not found", 404));

        if (!template.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Template access mismatch", 400);
        }

        accessValidator.validateWriteAccess(template.getWarehouse().getOwnerId(),
                template.getWarehouse().getOwnerType());

        template.update(command);
        template.updateTimestamp(timeProvider.now());
        
        return blockTemplateRepository.save(template);
    }

    @Transactional
    public void deleteBlockTemplate(UUID warehouseId, UUID id) {
        BlockTemplate template = blockTemplateRepository.findById(id)
                .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND", "Block template not found", 404));

        if (!template.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Template access mismatch", 400);
        }

        accessValidator.validateWriteAccess(template.getWarehouse().getOwnerId(),
                template.getWarehouse().getOwnerType());

        OffsetDateTime now = timeProvider.now();
        template.markAsDeleted(now);
        template.updateTimestamp(now);
        blockTemplateRepository.save(template);
    }

    private Warehouse getWarehouseAndValidateRead(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found", 404));
        accessValidator.validateReadAccess(warehouse.getOwnerId(), warehouse.getOwnerType());
        return warehouse;
    }
}
