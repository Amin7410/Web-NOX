package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.AssetCollectionRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetCollectionService {

    private final AssetCollectionRepository collectionRepository;
    private final WarehouseService warehouseService;

    @Transactional
    public AssetCollection createCollection(UUID warehouseId, String name, UUID parentCollectionId) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());

        if (collectionRepository.findByWarehouseIdAndName(warehouseId, name).isPresent()) {
            throw new DomainException("COLLECTION_EXISTS", "Collection with this name already exists in the warehouse",
                    400);
        }

        AssetCollection parent = null;
        if (parentCollectionId != null) {
            parent = collectionRepository.findById(parentCollectionId)
                    .orElseThrow(() -> new DomainException("PARENT_NOT_FOUND", "Parent collection not found", 404));
            if (!parent.getWarehouse().getId().equals(warehouseId)) {
                throw new DomainException("INVALID_PARENT", "Parent collection belongs to a different warehouse", 400);
            }
        }

        AssetCollection collection = AssetCollection.builder()
                .warehouse(warehouse)
                .name(name)
                .parentCollection(parent)
                .build();

        return collectionRepository.save(collection);
    }

    public AssetCollection getCollection(UUID warehouseId, UUID id) {
        AssetCollection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new DomainException("COLLECTION_NOT_FOUND", "Collection not found", 404));

        if (!collection.getWarehouse().getId().equals(warehouseId)) {
            throw new DomainException("INVALID_WAREHOUSE", "Collection does not belong to the specified warehouse",
                    400);
        }

        warehouseService.validateReadOwnership(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());
        return collection;
    }

    public List<AssetCollection> getRootCollections(UUID warehouseId) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateReadOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
        return collectionRepository.findByWarehouseIdAndParentCollectionIsNull(warehouseId);
    }

    public List<AssetCollection> getChildCollections(UUID warehouseId, UUID parentId) {
        Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
        warehouseService.validateReadOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
        return collectionRepository.findByWarehouseIdAndParentCollectionId(warehouseId, parentId);
    }

    @Transactional
    public AssetCollection updateCollectionParent(UUID warehouseId, UUID id, UUID newParentId) {
        AssetCollection collection = getCollection(warehouseId, id);

        warehouseService.validateWriteOwnership(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());

        if (newParentId != null) {
            // Re-validate and fetch the parent collection
            AssetCollection parent = getCollection(warehouseId, newParentId);
            if (!parent.getWarehouse().getId().equals(collection.getWarehouse().getId())) {
                throw new DomainException("INVALID_PARENT", "Parent collection belongs to a different warehouse", 400);
            }
            validateNoCyclicDependency(id, newParentId);
            collection.setParentCollection(parent);
        } else {
            collection.setParentCollection(null);
        }

        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(UUID warehouseId, UUID id) {
        AssetCollection collection = getCollection(warehouseId, id);
        warehouseService.validateWriteOwnership(collection.getWarehouse().getOwnerId(),
                collection.getWarehouse().getOwnerType());

        List<AssetCollection> children = collectionRepository
                .findByWarehouseIdAndParentCollectionId(collection.getWarehouse().getId(), id);
        if (!children.isEmpty()) {
            throw new DomainException("COLLECTION_NOT_EMPTY", "Cannot delete collection with child collections", 400);
        }

        collection.softDelete();
        collectionRepository.save(collection);
    }

    public void validateNoCyclicDependency(UUID collectionIdToUpdate, UUID newParentId) {
        if (newParentId == null || collectionIdToUpdate == null) {
            return;
        }

        if (collectionIdToUpdate.equals(newParentId)) {
            throw new DomainException("CYCLIC_DEPENDENCY", "A collection cannot be its own parent", 400);
        }

        // Trace up the parent chain
        UUID currentParentId = newParentId;
        while (currentParentId != null) {
            AssetCollection parent = collectionRepository.findById(currentParentId).orElse(null);
            if (parent == null) {
                break;
            }
            if (collectionIdToUpdate.equals(parent.getId())) {
                throw new DomainException("CYCLIC_DEPENDENCY", "Setting this parent would create a cyclic dependency",
                        400);
            }
            currentParentId = parent.getParentCollection() != null ? parent.getParentCollection().getId() : null;
        }
    }
}
