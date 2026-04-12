package com.nox.platform.module.warehouse.service.listener;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        // Given
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "test@example.com");
        when(warehouseRepository.findByOwnerIdAndOwnerType(userId, OwnerType.USER)).thenReturn(Optional.empty());

        // When
        eventListener.onUserCreated(event);

        // Then
        verify(warehouseService).internalCreateWarehouse(eq(userId), eq(OwnerType.USER), anyString(), eq(false));
    }

    @Test
    @DisplayName("Should skip creation on UserCreatedEvent if warehouse already exists (Idempotency)")
    void shouldSkipIfUserWarehouseExists() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(userId, "test@example.com");
        when(warehouseRepository.findByOwnerIdAndOwnerType(userId, OwnerType.USER))
                .thenReturn(Optional.of(Warehouse.builder().build()));

        // When
        eventListener.onUserCreated(event);

        // Then
        verify(warehouseService, never()).internalCreateWarehouse(any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should auto-create org warehouse on OrganizationCreatedEvent")
    void shouldCreateWarehouseOnOrgCreated() {
        // Given
        UUID orgId = UUID.randomUUID();
        OrganizationCreatedEvent event = new OrganizationCreatedEvent(orgId, UUID.randomUUID());
        when(warehouseRepository.findByOwnerIdAndOwnerType(orgId, OwnerType.ORG)).thenReturn(Optional.empty());

        // When
        eventListener.onOrganizationCreated(event);

        // Then
        verify(warehouseService).internalCreateWarehouse(eq(orgId), eq(OwnerType.ORG), anyString(), eq(false));
    }
}
