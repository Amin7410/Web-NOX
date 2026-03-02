package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BlockTemplateRepository blockTemplateRepository;
    private final InvaderDefinitionRepository invaderDefinitionRepository;
    private final OrgMemberRepository orgMemberRepository;

    @Transactional
    public Warehouse createWarehouse(UUID ownerId, OwnerType ownerType, String name, boolean isSystem) {
        validateWriteOwnership(ownerId, ownerType);

        if (warehouseRepository.findByOwnerIdAndOwnerType(ownerId, ownerType).isPresent()) {
            throw new DomainException("WAREHOUSE_EXISTS", "Warehouse already exists for this owner", 400);
        }

        Warehouse warehouse = Warehouse.builder()
                .ownerId(ownerId)
                .ownerType(ownerType)
                .name(name)
                .isSystem(isSystem)
                .build();

        return warehouseRepository.save(warehouse);
    }

    public Warehouse getWarehouseForOwner(UUID ownerId, OwnerType ownerType) {
        return warehouseRepository.findByOwnerIdAndOwnerType(ownerId, ownerType)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found", 404));
    }

    public Warehouse getWarehouseById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new DomainException("WAREHOUSE_NOT_FOUND", "Warehouse not found", 404));
        validateReadOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
        return warehouse;
    }

    public List<Warehouse> getWarehousesByOwner(UUID ownerId) {
        // For simplicity, we just infer the context type based on whether the ownerId
        // matches current user or if they are in an Org.
        // It's safer to always validate. However, to correctly validate we need to
        // check if the ownerId is an ORG or USER.
        // Let's get the warehouse list first, then validate the first one OR validate
        // the user context dynamically.
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new DomainException("UNAUTHORIZED", "User not authenticated", 401);
        }

        if (currentUserId.equals(ownerId)) {
            validateReadOwnership(ownerId, OwnerType.USER);
        } else {
            validateReadOwnership(ownerId, OwnerType.ORG);
        }

        return warehouseRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public void deleteWarehouse(UUID id) {
        Warehouse warehouse = getWarehouseById(id);
        validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());

        if (warehouse.isSystem()) {
            throw new DomainException("SYSTEM_WAREHOUSE", "Cannot delete system warehouse", 400);
        }

        internalDeleteWarehouse(id);
    }

    @Transactional
    public void internalDeleteWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElse(null);
        if (warehouse == null)
            return;

        // 1. Soft delete the warehouse
        warehouse.softDelete();
        warehouseRepository.save(warehouse);

        // 2. Cascade soft delete to all children elements
        blockTemplateRepository.softDeleteByWarehouseId(id);
        invaderDefinitionRepository.softDeleteByWarehouseId(id);
    }

    public void validateReadOwnership(UUID targetOwnerId, OwnerType ownerType) {
        validateOwnershipWithRole(targetOwnerId, ownerType, false);
    }

    public void validateWriteOwnership(UUID targetOwnerId, OwnerType ownerType) {
        validateOwnershipWithRole(targetOwnerId, ownerType, true);
    }

    private void validateOwnershipWithRole(UUID targetOwnerId, OwnerType ownerType, boolean writeRequired) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new DomainException("UNAUTHORIZED", "User not authenticated", 401);
        }

        if (ownerType == OwnerType.USER) {
            if (!currentUserId.equals(targetOwnerId)) {
                throw new DomainException("FORBIDDEN",
                        "You do not have permission to access or modify this user's warehouse", 403);
            }
        } else if (ownerType == OwnerType.ORG) {
            var member = orgMemberRepository.findByOrganizationIdAndUserId(targetOwnerId, currentUserId)
                    .orElseThrow(() -> new DomainException("FORBIDDEN",
                            "You do not have permission. You are not a member of this organization", 403));

            if (writeRequired) {
                boolean hasWritePerm = member.getRole().getPermissions().contains("*") ||
                        member.getRole().getPermissions().contains("workspace:manage");
                if (!hasWritePerm) {
                    throw new DomainException("FORBIDDEN",
                            "Insufficient permissions. Required permission: 'workspace:manage'",
                            403);
                }
            }
        }
    }
}
