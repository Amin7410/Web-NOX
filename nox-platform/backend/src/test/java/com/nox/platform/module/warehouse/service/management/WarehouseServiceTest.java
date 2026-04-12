package com.nox.platform.module.warehouse.service.management;

import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
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
    private BlockTemplateRepository blockTemplateRepository;
    @Mock
    private InvaderDefinitionRepository invaderDefinitionRepository;
    @Mock
    private OrgMemberRepository orgMemberRepository;
    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private WarehouseService warehouseService;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;
    private final UUID currentUserId = UUID.randomUUID();
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        lenient().when(timeProvider.now()).thenReturn(now);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Nested
    @DisplayName("Creation & Ownership Scenarios")
    class CreationTests {

        @Test
        @DisplayName("Should successfully create a personal warehouse")
        void shouldCreatePersonalWarehouse() {
            // When
            warehouseService.createWarehouse(currentUserId, OwnerType.USER, "My Store", false);

            // Then
            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should throw exception when creating a warehouse for someone else")
        void shouldThrowWhenCreatingForOtherUser() {
            UUID otherUserId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> warehouseService.createWarehouse(otherUserId, OwnerType.USER, "Fail", false))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        }

        @Test
        @DisplayName("Should throw exception if warehouse already exists")
        void shouldThrowIfWarehouseExists() {
            // Given
            when(warehouseRepository.findByOwnerIdAndOwnerType(currentUserId, OwnerType.USER))
                    .thenReturn(Optional.of(Warehouse.builder().build()));

            // When & Then
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
            // Given
            Role adminRole = Role.builder().permissions(List.of("workspace:manage")).build();
            OrgMember member = OrgMember.builder().role(adminRole).build();
            
            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId))
                    .thenReturn(Optional.of(member));

            // When
            warehouseService.createWarehouse(orgId, OwnerType.ORG, "Org Store", false);

            // Then
            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("Should deny Org member without manage permission")
        void shouldDenyLesserMember() {
            // Given
            Role guestRole = Role.builder().permissions(List.of("workspace:read")).build();
            OrgMember member = OrgMember.builder().role(guestRole).build();
            
            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId))
                    .thenReturn(Optional.of(member));

            // When & Then
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
            // Given
            UUID warehouseId = UUID.randomUUID();
            Warehouse warehouse = Warehouse.builder().id(warehouseId).ownerId(currentUserId).ownerType(OwnerType.USER).build();
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

            // When
            warehouseService.deleteWarehouse(warehouseId);

            // Then
            assertThat(warehouse.getDeletedAt()).isEqualTo(now);
            verify(blockTemplateRepository).softDeleteByWarehouseId(warehouseId, now);
            verify(invaderDefinitionRepository).softDeleteByWarehouseId(warehouseId, now);
        }

        @Test
        @DisplayName("Should prevent deleting system warehouses")
        void shouldPreventDeletingSystemWarehouse() {
            // Given
            UUID warehouseId = UUID.randomUUID();
            Warehouse systemWarehouse = Warehouse.builder()
                    .id(warehouseId)
                    .ownerId(currentUserId)
                    .ownerType(OwnerType.USER)
                    .isSystem(true)
                    .build();
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(systemWarehouse));

            // When & Then
            assertThatThrownBy(() -> warehouseService.deleteWarehouse(warehouseId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "SYSTEM_WAREHOUSE");
        }
    }
}
