package com.nox.platform.module.warehouse.service.listener;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.WarehouseEventListener;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.shared.event.OrganizationCreatedEvent;
import com.nox.platform.shared.event.UserCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseEventListener Unit Tests")
class WarehouseEventListenerTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseEventListener eventListener;

    @Test
    @DisplayName("Should auto-create personal warehouse on UserCreatedEvent")
    void shouldCreateWarehouseOnUserCreated() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "test@example.com");
        when(warehouseService.existsByOwner(userId, OwnerType.USER)).thenReturn(false);

        eventListener.onUserCreated(event);

        verify(warehouseService).internalCreateWarehouse(eq(userId), eq(OwnerType.USER), anyString(), eq(false));
    }

    @Test
    @DisplayName("Should skip creation on UserCreatedEvent if warehouse already exists (Idempotency)")
    void shouldSkipIfUserWarehouseExists() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "test@example.com");
        when(warehouseService.existsByOwner(userId, OwnerType.USER)).thenReturn(true);

        eventListener.onUserCreated(event);

        verify(warehouseService, never()).internalCreateWarehouse(any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should auto-create org warehouse on OrganizationCreatedEvent")
    void shouldCreateWarehouseOnOrgCreated() {
        UUID orgId = UUID.randomUUID();
        OrganizationCreatedEvent event = new OrganizationCreatedEvent(orgId, UUID.randomUUID());
        when(warehouseService.existsByOwner(orgId, OwnerType.ORG)).thenReturn(false);

        eventListener.onOrganizationCreated(event);

        verify(warehouseService).internalCreateWarehouse(eq(orgId), eq(OwnerType.ORG), anyString(), eq(false));
    }
}
