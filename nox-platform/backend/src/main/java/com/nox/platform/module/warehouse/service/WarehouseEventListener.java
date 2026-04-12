package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.shared.event.OrganizationDeletedEvent;
import com.nox.platform.shared.event.UserDeletedEvent;
import com.nox.platform.shared.event.OrganizationCreatedEvent;
import com.nox.platform.shared.event.UserCreatedEvent;
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
        log.info("Processing UserDeletedEvent for user: {}", event.userId());
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findByOwnerIdAndOwnerType(event.userId(),
                OwnerType.USER);

        optionalWarehouse.ifPresent(warehouse -> {
            log.info("Soft deleting warehouse: {}", warehouse.getId());
            warehouseService.internalDeleteWarehouse(warehouse.getId());
        });
    }

    @EventListener
    public void onOrganizationDeleted(OrganizationDeletedEvent event) {
        log.info("Processing OrganizationDeletedEvent for org: {}", event.organizationId());
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findByOwnerIdAndOwnerType(event.organizationId(),
                OwnerType.ORG);

        optionalWarehouse.ifPresent(warehouse -> {
            log.info("Soft deleting warehouse: {}", warehouse.getId());
            warehouseService.internalDeleteWarehouse(warehouse.getId());
        });
    }

    @EventListener
    public void onOrganizationCreated(OrganizationCreatedEvent event) {
        log.info("Processing OrganizationCreatedEvent for org: {}", event.organizationId());
        
        if (warehouseService.existsByOwner(event.organizationId(), OwnerType.ORG)) {
            log.info("Warehouse already exists for org: {}", event.organizationId());
            return;
        }

        try {
            warehouseService.internalCreateWarehouse(
                event.organizationId(), 
                OwnerType.ORG, 
                "Default Warehouse", 
                false
            );
            log.info("Auto-created Warehouse for org: {}", event.organizationId());
        } catch (Exception e) {
            log.error("Failed to auto-create Warehouse for org: {}. Error: {}", event.organizationId(), e.getMessage());
        }
    }

    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        log.info("Processing UserCreatedEvent for user: {}", event.userId());

        if (warehouseService.existsByOwner(event.userId(), OwnerType.USER)) {
            log.info("Personal warehouse already exists for user: {}", event.userId());
            return;
        }

        try {
            warehouseService.internalCreateWarehouse(
                event.userId(), 
                OwnerType.USER, 
                "Personal Warehouse", 
                false
            );
            log.info("Auto-created Personal Warehouse for user: {}", event.userId());
        } catch (Exception e) {
            log.error("Failed to auto-create Personal Warehouse for user: {}. Error: {}", event.userId(), e.getMessage());
        }
    }
}
