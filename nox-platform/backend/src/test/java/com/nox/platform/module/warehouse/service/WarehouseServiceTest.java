package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.module.warehouse.infrastructure.WarehouseRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private BlockTemplateRepository blockTemplateRepository;
    @Mock
    private InvaderDefinitionRepository invaderDefinitionRepository;
    @Mock
    private OrgMemberRepository orgMemberRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private UUID currentUserId;
    private UUID orgId;
    private Warehouse userWarehouse;
    private Warehouse orgWarehouse;

    @BeforeEach
    void setUp() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        currentUserId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        userWarehouse = Warehouse.builder()
                .ownerId(currentUserId)
                .ownerType(OwnerType.USER)
                .name("My Personal Vault")
                .isSystem(false)
                .build();
        userWarehouse.setId(UUID.randomUUID());

        orgWarehouse = Warehouse.builder()
                .ownerId(orgId)
                .ownerType(OwnerType.ORG)
                .name("Corp Main Storage")
                .isSystem(true)
                .build();
        orgWarehouse.setId(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        securityUtilMockedStatic.close();
    }

    // --- Create Tests ---
    @Test
    void createWarehouse_UserOwner_Success() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        when(warehouseRepository.findByOwnerIdAndOwnerType(currentUserId, OwnerType.USER)).thenReturn(Optional.empty());
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        Warehouse result = warehouseService.createWarehouse(currentUserId, OwnerType.USER, "New Vault", false);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Vault");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_AlreadyExists_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        when(warehouseRepository.findByOwnerIdAndOwnerType(currentUserId, OwnerType.USER))
                .thenReturn(Optional.of(userWarehouse));

        assertThatThrownBy(() -> warehouseService.createWarehouse(currentUserId, OwnerType.USER, "New Vault", false))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already exists");
    }

    // --- Read Authorization Tests ---
    @Test
    void getWarehouseById_UserOwner_MatchedId_Success() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        when(warehouseRepository.findById(userWarehouse.getId())).thenReturn(Optional.of(userWarehouse));

        Warehouse result = warehouseService.getWarehouseById(userWarehouse.getId());

        assertThat(result.getId()).isEqualTo(userWarehouse.getId());
    }

    @Test
    void getWarehouseById_UserOwner_MismatchedId_ThrowsForbidden() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(UUID.randomUUID()); // different user
        when(warehouseRepository.findById(userWarehouse.getId())).thenReturn(Optional.of(userWarehouse));

        assertThatThrownBy(() -> warehouseService.getWarehouseById(userWarehouse.getId()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("status", 403);
    }

    @Test
    void getWarehouseById_OrgOwner_MemberExists_Success() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        when(warehouseRepository.findById(orgWarehouse.getId())).thenReturn(Optional.of(orgWarehouse));
        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId))
                .thenReturn(Optional.of(mock(OrgMember.class)));

        Warehouse result = warehouseService.getWarehouseById(orgWarehouse.getId());

        assertThat(result.getId()).isEqualTo(orgWarehouse.getId());
    }

    @Test
    void getWarehouseById_OrgOwner_NotMember_ThrowsForbidden() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
        when(warehouseRepository.findById(orgWarehouse.getId())).thenReturn(Optional.of(orgWarehouse));
        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById(orgWarehouse.getId()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("status", 403);
    }

    // --- Write Authorization Tests ---
    @Test
    void checkWriteAccess_OrgOwner_RoleAdmin_Success() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        OrgMember member = mock(OrgMember.class);
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(List.of("workspace:manage"));
        when(member.getRole()).thenReturn(role);

        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId)).thenReturn(Optional.of(member));

        // Testing the public write validation directly
        warehouseService.validateWriteOwnership(orgId, OwnerType.ORG); // should pass without throwing
    }

    @Test
    void checkWriteAccess_OrgOwner_RoleMember_ThrowsForbidden() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        OrgMember member = mock(OrgMember.class);
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(List.of("workspace:read"));
        when(member.getRole()).thenReturn(role);

        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> warehouseService.validateWriteOwnership(orgId, OwnerType.ORG))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Required permission: 'workspace:manage'");
    }

    // --- Internal Delete logic bypassed check ---
    @Test
    void internalDeleteWarehouse_SoftDeletesCascades() {
        when(warehouseRepository.findById(userWarehouse.getId())).thenReturn(Optional.of(userWarehouse));

        warehouseService.internalDeleteWarehouse(userWarehouse.getId());

        assertThat(userWarehouse.getDeletedAt()).isNotNull();
        verify(warehouseRepository).save(userWarehouse);
        verify(blockTemplateRepository).softDeleteByWarehouseId(userWarehouse.getId());
        verify(invaderDefinitionRepository).softDeleteByWarehouseId(userWarehouse.getId());
    }

    @Test
    void deleteWarehouse_PreventsSystemDeletion() {
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);

        OrgMember member = mock(OrgMember.class);
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(List.of("*"));
        when(member.getRole()).thenReturn(role);

        when(warehouseRepository.findById(orgWarehouse.getId())).thenReturn(Optional.of(orgWarehouse));
        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, currentUserId)).thenReturn(Optional.of(member));

        // orgWarehouse is setup as System warehouse
        assertThatThrownBy(() -> warehouseService.deleteWarehouse(orgWarehouse.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot delete system warehouse");
    }
}
