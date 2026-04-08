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

    @EventListener
    public void onOrganizationCreated(com.nox.platform.shared.event.OrganizationCreatedEvent event) {
        log.info("Received OrganizationCreatedEvent for org {}. Creating default warehouse...", event.organizationId());
        
        // Ensure idempotency: Check if warehouse already exists
        boolean exists = warehouseRepository.findByOwnerIdAndOwnerType(event.organizationId(), OwnerType.ORG).isPresent();
        if (exists) {
            log.info("Warehouse already exists for org {}. Skipping creation.", event.organizationId());
            return;
        }

        try {
            // We use internalCreateWarehouse to bypass SecurityContext/RBAC during auto-provisioning
            warehouseService.internalCreateWarehouse(
                event.organizationId(), 
                OwnerType.ORG, 
                "Default Warehouse", 
                false
            );
            log.info("Successfully auto-created Warehouse for org: {}", event.organizationId());
        } catch (Exception e) {
            log.error("Failed to auto-create Warehouse for org: {}. Error: {}", event.organizationId(), e.getMessage());
        }
    }

    @EventListener
    public void onUserCreated(com.nox.platform.shared.event.UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for user {}. Creating personal warehouse...", event.userId());

        // Ensure idempotency: Check if warehouse already exists
        boolean exists = warehouseRepository.findByOwnerIdAndOwnerType(event.userId(), OwnerType.USER).isPresent();
        if (exists) {
            log.info("Personal warehouse already exists for user {}. Skipping creation.", event.userId());
            return;
        }

        try {
            // We use internalCreateWarehouse to bypass SecurityContext/RBAC during auto-provisioning
            warehouseService.internalCreateWarehouse(
                event.userId(), 
                OwnerType.USER, 
                "Personal Warehouse", 
                false
            );
            log.info("Successfully auto-created Personal Warehouse for user: {}", event.userId());
        } catch (Exception e) {
            log.error("Failed to auto-create Personal Warehouse for user: {}. Error: {}", event.userId(), e.getMessage());
        }
    }
}
