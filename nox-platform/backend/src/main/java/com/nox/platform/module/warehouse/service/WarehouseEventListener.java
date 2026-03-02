package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.shared.event.OrganizationDeletedEvent;
import com.nox.platform.shared.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseEventListener {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseService warehouseService;

    @EventListener
    public void onUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for user {}. Checking for associated warehouse...", event.userId());
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findByOwnerIdAndOwnerType(event.userId(),
                OwnerType.USER);

        optionalWarehouse.ifPresent(warehouse -> {
            log.info("Soft deleting warehouse {} due to user deletion", warehouse.getId());
            // Need to bypass RBAC validation in warehouseService.deleteWarehouse
            // So we might either do it directly or add an internal method.
            warehouseService.internalDeleteWarehouse(warehouse.getId());
        });
    }

    @EventListener
    public void onOrganizationDeleted(OrganizationDeletedEvent event) {
        log.info("Received OrganizationDeletedEvent for org {}. Checking for associated warehouse...",
                event.organizationId());
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findByOwnerIdAndOwnerType(event.organizationId(),
                OwnerType.ORG);

        optionalWarehouse.ifPresent(warehouse -> {
            log.info("Soft deleting warehouse {} due to organization deletion", warehouse.getId());
            warehouseService.internalDeleteWarehouse(warehouse.getId());
        });
    }
}
