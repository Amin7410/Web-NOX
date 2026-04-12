package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.AssetCollectionRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.command.CreateCollectionCommand;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetCollectionService {

    private final AssetCollectionRepository collectionRepository;
    private final WarehouseRepository warehouseRepository;
    private final com.nox.platform.shared.abstraction.TimeProvider timeProvider;
    private final WarehouseAccessValidator accessValidator;

    @Transactional
    public AssetCollection createCollection(CreateCollectionCommand command) {
        Warehouse warehouse = getWarehouseAndValidateRead(command.warehouseId());
        accessValidator.validateWriteAccess(warehouse.getOwnerId(), warehouse.getOwnerType());

        if (collectionRepository.findByWarehouseIdAndName(command.warehouseId(), command.name()).isPresent()) {
            throw new DomainException("COLLECTION_EXISTS", "Collection name exists in this warehouse");
        }

        AssetCollection parent = null;
        if (command.parentCollectionId() != null) {
            parent = getCollection(command.warehouseId(), command.parentCollectionId());
        }

        AssetCollection collection = AssetCollection.create(warehouse, parent, command.name(), timeProvider.now());
        return collectionRepository.save(collection);
    }

    public AssetCollection getCollection(UUID warehouseId, UUID id) {
        AssetCollection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new DomainException("COLLECTION_NOT_FOUND", "Collection not found"));

        if (!collection.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Collection access mismatch");
        }

        accessValidator.validateReadAccess(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());
        return collection;
    }

    public List<AssetCollection> getRootCollections(UUID warehouseId) {
        getWarehouseAndValidateRead(warehouseId);
        return collectionRepository.findByWarehouseIdAndParentCollectionIsNull(warehouseId);
    }

    public List<AssetCollection> getChildCollections(UUID warehouseId, UUID parentId) {
        getWarehouseAndValidateRead(warehouseId);
        return collectionRepository.findByWarehouseIdAndParentCollectionId(warehouseId, parentId);
    }

    @Transactional
    public AssetCollection updateCollectionParent(UUID warehouseId, UUID id, UUID newParentId) {
        AssetCollection collection = getCollection(warehouseId, id);
        accessValidator.validateWriteAccess(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());

        if (newParentId != null) {
            AssetCollection parent = getCollection(warehouseId, newParentId);
            validateNoCyclicDependency(id, newParentId);
            collection.changeParent(parent);
        } else {
            collection.changeParent(null);
        }

        collection.updateTimestamp(timeProvider.now());
        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(UUID warehouseId, UUID id) {
        AssetCollection collection = getCollection(warehouseId, id);
        accessValidator.validateWriteAccess(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());

        List<AssetCollection> children = collectionRepository
                .findByWarehouseIdAndParentCollectionId(collection.getWarehouse().getId(), id);
        if (!children.isEmpty()) {
            throw new DomainException("COLLECTION_NOT_EMPTY", "Cannot delete non-empty collection");
        }

        OffsetDateTime now = timeProvider.now();
        collection.markAsDeleted(now);
        collection.updateTimestamp(now);
        collectionRepository.save(collection);
    }

    @Transactional
    public void softDeleteAllByWarehouse(UUID warehouseId, OffsetDateTime now) {
        collectionRepository.softDeleteByWarehouseId(warehouseId, now);
    }

    private void validateNoCyclicDependency(UUID collectionIdToUpdate, UUID newParentId) {
        if (newParentId == null || collectionIdToUpdate == null) return;

        if (collectionIdToUpdate.equals(newParentId)) {
            throw new DomainException("CYCLIC_DEPENDENCY", "Self-parenting not allowed");
        }

        UUID currentParentId = newParentId;
        while (currentParentId != null) {
            AssetCollection parent = collectionRepository.findById(currentParentId).orElse(null);
            if (parent == null) break;
            
            if (collectionIdToUpdate.equals(parent.getId())) {
                throw new DomainException("CYCLIC_DEPENDENCY", "Circular dependency detected");
            }
            currentParentId = parent.getParentCollection() != null ? parent.getParentCollection().getId() : null;
        }
    }

    private Warehouse getWarehouseAndValidateRead(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found"));
        accessValidator.validateReadAccess(warehouse.getOwnerId(), warehouse.getOwnerType());
        return warehouse;
    }
}

