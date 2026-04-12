package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.shared.abstraction.SecurityProvider;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BlockTemplateService blockTemplateService;
    private final InvaderDefinitionService invaderDefinitionService;
    private final AssetCollectionService assetCollectionService;
    private final TimeProvider timeProvider;
    private final SecurityProvider securityProvider;
    private final WarehouseAccessValidator accessValidator;

    @Transactional
    public Warehouse createWarehouse(UUID ownerId, OwnerType ownerType, String name, boolean isSystem) {
        accessValidator.validateWriteAccess(ownerId, ownerType);
        if (existsByOwner(ownerId, ownerType)) {
            throw new DomainException("WAREHOUSE_EXISTS", "Warehouse already exists for this owner");
        }
        return internalCreateWarehouse(ownerId, ownerType, name, isSystem);
    }

    public boolean existsByOwner(UUID ownerId, OwnerType ownerType) {
        return warehouseRepository.findByOwnerIdAndOwnerType(ownerId, ownerType).isPresent();
    }

    @Transactional
    public Warehouse internalCreateWarehouse(UUID ownerId, OwnerType ownerType, String name, boolean isSystem) {
        Warehouse warehouse = Warehouse.create(ownerId, ownerType, name, isSystem, timeProvider.now());
        return warehouseRepository.save(warehouse);
    }

    public Warehouse getWarehouseForOwner(UUID ownerId, OwnerType ownerType) {
        return warehouseRepository.findByOwnerIdAndOwnerType(ownerId, ownerType)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found"));
    }

    public Warehouse getWarehouseById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found"));
        accessValidator.validateReadAccess(warehouse.getOwnerId(), warehouse.getOwnerType());
        return warehouse;
    }

    public List<Warehouse> getWarehousesByOwner(UUID ownerId) {
        UUID currentUserId = securityProvider.getCurrentUserId()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required"));

        OwnerType ownerType = currentUserId.equals(ownerId) ? OwnerType.USER : OwnerType.ORG;
        accessValidator.validateReadAccess(ownerId, ownerType);

        return warehouseRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public void deleteWarehouse(UUID id) {
        Warehouse warehouse = getWarehouseById(id);
        accessValidator.validateWriteAccess(warehouse.getOwnerId(), warehouse.getOwnerType());
        internalDeleteWarehouse(id);
    }

    @Transactional
    public void internalDeleteWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElse(null);
        if (warehouse == null) return;

        OffsetDateTime now = timeProvider.now();
        warehouse.markAsDeleted(now);
        warehouse.updateTimestamp(now);
        warehouseRepository.save(warehouse);

        blockTemplateService.softDeleteAllByWarehouse(id, now);
        invaderDefinitionService.softDeleteAllByWarehouse(id, now);
        assetCollectionService.softDeleteAllByWarehouse(id, now);
    }
}

