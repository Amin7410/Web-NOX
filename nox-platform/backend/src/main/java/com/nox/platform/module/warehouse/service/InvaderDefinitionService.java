package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.command.CreateInvaderDefinitionCommand;
import com.nox.platform.module.warehouse.service.command.UpdateInvaderDefinitionCommand;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.abstraction.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvaderDefinitionService {

    private final InvaderDefinitionRepository invaderDefinitionRepository;
    private final WarehouseRepository warehouseRepository;
    private final AssetCollectionService collectionService;
    private final TimeProvider timeProvider;
    private final WarehouseAccessValidator accessValidator;

    @Transactional
    public InvaderDefinition createInvaderDefinition(CreateInvaderDefinitionCommand command) {
        Warehouse warehouse = getWarehouseAndValidateRead(command.warehouseId());
        accessValidator.validateWriteAccess(warehouse.getOwnerId(), warehouse.getOwnerType());

        if (invaderDefinitionRepository.findByWarehouseIdAndCode(command.warehouseId(), command.code()).isPresent()) {
            throw new DomainException("INVADER_CODE_EXISTS", "Invader code already exists in this warehouse");
        }

        AssetCollection collection = null;
        if (command.collectionId() != null) {
            collection = collectionService.getCollection(command.warehouseId(), command.collectionId());
        }

        InvaderDefinition definition = InvaderDefinition.create(warehouse, collection, command, timeProvider.now());
        return invaderDefinitionRepository.save(definition);
    }

    @Transactional
    public void softDeleteAllByWarehouse(UUID warehouseId, OffsetDateTime now) {
        invaderDefinitionRepository.softDeleteByWarehouseId(warehouseId, now);
    }

    public List<InvaderDefinition> getInvaderDefinitionsByWarehouse(UUID warehouseId) {
        getWarehouseAndValidateRead(warehouseId);
        return invaderDefinitionRepository.findByWarehouseId(warehouseId);
    }

    @Transactional
    public InvaderDefinition updateInvaderDefinition(UUID warehouseId, UUID id, UpdateInvaderDefinitionCommand command) {
        InvaderDefinition definition = invaderDefinitionRepository.findById(id)
                .orElseThrow(() -> new DomainException("INVADER_NOT_FOUND", "Invader definition not found"));

        if (!definition.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Invader definition access mismatch");
        }

        accessValidator.validateWriteAccess(definition.getWarehouse().getOwnerId(),
                definition.getWarehouse().getOwnerType());

        definition.update(command);
        definition.updateTimestamp(timeProvider.now());
        
        return invaderDefinitionRepository.save(definition);
    }

    @Transactional
    public void deleteInvaderDefinition(UUID warehouseId, UUID id) {
        InvaderDefinition definition = invaderDefinitionRepository.findById(id)
                .orElseThrow(() -> new DomainException("INVADER_NOT_FOUND", "Invader definition not found"));

        if (!definition.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Invader definition access mismatch");
        }

        accessValidator.validateWriteAccess(definition.getWarehouse().getOwnerId(),
                definition.getWarehouse().getOwnerType());

        OffsetDateTime now = timeProvider.now();
        definition.markAsDeleted(now);
        definition.updateTimestamp(now);
        invaderDefinitionRepository.save(definition);
    }

    private Warehouse getWarehouseAndValidateRead(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found"));
        accessValidator.validateReadAccess(warehouse.getOwnerId(), warehouse.getOwnerType());
        return warehouse;
    }
}

