package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.shared.event.OrganizationDeletedEvent;
import com.nox.platform.shared.event.UserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseEventListenerTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseEventListener eventListener;

    @Test
    void onUserDeleted_CallsInternalDeleteWhenFound() {
        UUID userId = UUID.randomUUID();
        Warehouse warehouse = Warehouse.builder().ownerId(userId).ownerType(OwnerType.USER).build();
        warehouse.setId(UUID.randomUUID());

        when(warehouseRepository.findByOwnerIdAndOwnerType(userId, OwnerType.USER)).thenReturn(Optional.of(warehouse));

        eventListener.onUserDeleted(new UserDeletedEvent(userId));

        verify(warehouseService).internalDeleteWarehouse(warehouse.getId());
    }

    @Test
    void onUserDeleted_DoesNothingIfNotFound() {
        UUID userId = UUID.randomUUID();
        when(warehouseRepository.findByOwnerIdAndOwnerType(userId, OwnerType.USER)).thenReturn(Optional.empty());

        eventListener.onUserDeleted(new UserDeletedEvent(userId));

        verify(warehouseService, never()).internalDeleteWarehouse(any(UUID.class));
    }

    @Test
    void onOrganizationDeleted_CallsInternalDeleteWhenFound() {
        UUID orgId = UUID.randomUUID();
        Warehouse warehouse = Warehouse.builder().ownerId(orgId).ownerType(OwnerType.ORG).build();
        warehouse.setId(UUID.randomUUID());

        when(warehouseRepository.findByOwnerIdAndOwnerType(orgId, OwnerType.ORG)).thenReturn(Optional.of(warehouse));

        eventListener.onOrganizationDeleted(new OrganizationDeletedEvent(orgId));

        verify(warehouseService).internalDeleteWarehouse(warehouse.getId());
    }
}
