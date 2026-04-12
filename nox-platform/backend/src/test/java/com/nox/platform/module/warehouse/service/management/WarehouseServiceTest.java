package com.nox.platform.module.warehouse.service.management;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.AssetCollectionService;
import com.nox.platform.module.warehouse.service.BlockTemplateService;
import com.nox.platform.module.warehouse.service.InvaderDefinitionService;
import com.nox.platform.module.warehouse.service.WarehouseAccessValidator;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseService Unit Tests")
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private BlockTemplateService blockTemplateService;
    @Mock
    private InvaderDefinitionService invaderDefinitionService;
    @Mock
    private AssetCollectionService assetCollectionService;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private SecurityProvider securityProvider;
    @Mock
    private WarehouseAccessValidator accessValidator;

    @InjectMocks
    private WarehouseService warehouseService;

    private final UUID currentUserId = UUID.randomUUID();
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.now()).thenReturn(now);
        lenient().when(securityProvider.getCurrentUserId()).thenReturn(Optional.of(currentUserId));
        lenient().when(warehouseRepository.findByOwnerIdAndOwnerType(any(), any())).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("Creation & Ownership Scenarios")
    class CreationTests {

        @Test
        @DisplayName("Should successfully create a personal warehouse")
        void shouldCreatePersonalWarehouse() {
            warehouseService.createWarehouse(currentUserId, OwnerType.USER, "My Store", false);

            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should throw exception when creating a warehouse for someone else")
        void shouldThrowWhenCreatingForOtherUser() {
            UUID otherUserId = UUID.randomUUID();
            doThrow(new DomainException("FORBIDDEN", "User warehouse access denied", 403))
                    .when(accessValidator).validateWriteAccess(otherUserId, OwnerType.USER);

            assertThatThrownBy(() -> warehouseService.createWarehouse(otherUserId, OwnerType.USER, "Fail", false))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        }

        @Test
        @DisplayName("Should throw exception if warehouse already exists")
        void shouldThrowIfWarehouseExists() {
            when(warehouseRepository.findByOwnerIdAndOwnerType(currentUserId, OwnerType.USER))
                    .thenReturn(Optional.of(Warehouse.builder().build()));

            assertThatThrownBy(() -> warehouseService.createWarehouse(currentUserId, OwnerType.USER, "Double", false))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "WAREHOUSE_EXISTS");
        }
    }

    @Nested
    @DisplayName("Organization Access (RBAC) Scenarios")
    class OrgAccessTests {

        private final UUID orgId = UUID.randomUUID();

        @Test
        @DisplayName("Should allow Org member with manage permission to create warehouse")
        void shouldAllowOrgAdminToCreate() {
            warehouseService.createWarehouse(orgId, OwnerType.ORG, "Org Store", false);

            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should deny Org member without manage permission")
        void shouldDenyLesserMember() {
            doThrow(new DomainException("FORBIDDEN", "Insufficient organization permissions", 403))
                    .when(accessValidator).validateWriteAccess(orgId, OwnerType.ORG);

            assertThatThrownBy(() -> warehouseService.createWarehouse(orgId, OwnerType.ORG, "Forbidden", false))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        }
    }

    @Nested
    @DisplayName("Deletion Scenarios")
    class DeletionTests {

        @Test
        @DisplayName("Should soft delete and cascade to child resources")
        void shouldDeleteAndCascade() {
            UUID warehouseId = UUID.randomUUID();
            Warehouse warehouse = Warehouse.builder()
                    .id(warehouseId).ownerId(currentUserId).ownerType(OwnerType.USER).build();
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

            warehouseService.deleteWarehouse(warehouseId);

            assertThat(warehouse.getDeletedAt()).isEqualTo(now);
            verify(blockTemplateService).softDeleteAllByWarehouse(warehouseId, now);
            verify(invaderDefinitionService).softDeleteAllByWarehouse(warehouseId, now);
        }

        @Test
        @DisplayName("Should prevent deleting system warehouses")
        void shouldPreventDeletingSystemWarehouse() {
            UUID warehouseId = UUID.randomUUID();
            Warehouse systemWarehouse = Warehouse.builder()
                    .id(warehouseId).ownerId(currentUserId).ownerType(OwnerType.USER).isSystem(true).build();
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(systemWarehouse));

            assertThatThrownBy(() -> warehouseService.deleteWarehouse(warehouseId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "SYSTEM_WAREHOUSE");
        }
    }
}
