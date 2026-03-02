package com.nox.platform.module.tenant.service;

import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrgMemberRepository orgMemberRepository;

    @InjectMocks
    private RoleService roleService;

    private Organization organization;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        organization = Organization.builder()
                .name("Test Org")
                .slug("test-org")
                .build();
        organization.setId(orgId);
    }

    @Test
    void createRole_Success() {
        String roleName = "EDITOR";
        List<String> permissions = List.of("post:write", "post:read");

        when(roleRepository.existsByOrganizationIdAndName(orgId, roleName)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> {
            Role role = i.getArgument(0);
            role.setId(UUID.randomUUID());
            return role;
        });

        Role role = roleService.createRole(organization, roleName, permissions, 10);

        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo(roleName);
        assertThat(role.getPermissions()).containsExactlyElementsOf(permissions);
        assertThat(role.getOrganization().getId()).isEqualTo(orgId);
    }

    @Test
    void createRole_NameExists_ThrowsException() {
        String roleName = "ADMIN";
        when(roleRepository.existsByOrganizationIdAndName(orgId, roleName)).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(organization, roleName, List.of(), 50))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Role name already exists");
    }

    @Test
    void getRolesByOrganization_Success() {
        Role role = Role.builder().organization(organization).name("ADMIN").build();
        when(roleRepository.findByOrganizationId(orgId)).thenReturn(List.of(role));

        List<Role> roles = roleService.getRolesByOrganization(orgId);

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("ADMIN");
    }

    @Test
    void getRoleByName_Success() {
        String roleName = "ADMIN";
        Role role = Role.builder().organization(organization).name(roleName).build();
        when(roleRepository.findByOrganizationIdAndName(orgId, roleName)).thenReturn(Optional.of(role));

        Role result = roleService.getRoleByName(orgId, roleName);

        assertThat(result.getName()).isEqualTo(roleName);
    }

    @Test
    void getRoleByName_NotFound_ThrowsException() {
        when(roleRepository.findByOrganizationIdAndName(orgId, "GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleByName(orgId, "GHOST"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Role not found in this organization");
    }

    @Test
    void updatePermissions_Success() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.builder().organization(organization).name("EDITOR").permissions(List.of("read")).build();
        role.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        List<String> newPermissions = List.of("read", "write");
        Role updatedRole = roleService.updatePermissions(roleId, newPermissions);

        assertThat(updatedRole.getPermissions()).containsExactlyElementsOf(newPermissions);
    }

    @Test
    void updatePermissions_OwnerRole_ThrowsException() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.builder().organization(organization).name("OWNER").permissions(List.of("*")).build();
        role.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.updatePermissions(roleId, List.of("some:perm")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot modify permissions of the OWNER role");
    }

    @Test
    void deleteRole_Success() {
        String roleName = "EDITOR";
        Role role = Role.builder().organization(organization).name(roleName).build();

        when(roleRepository.findByOrganizationIdAndName(orgId, roleName)).thenReturn(Optional.of(role));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(orgId, roleName)).thenReturn(0L);

        roleService.deleteRole(orgId, roleName);

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_OwnerRole_ThrowsException() {
        String roleName = "OWNER";
        Role role = Role.builder().organization(organization).name(roleName).build();

        when(roleRepository.findByOrganizationIdAndName(orgId, roleName)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.deleteRole(orgId, roleName))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("The OWNER role cannot be deleted");

        verify(roleRepository, never()).delete(any());
    }

    @Test
    void deleteRole_InUse_ThrowsException() {
        String roleName = "EDITOR";
        Role role = Role.builder().organization(organization).name(roleName).build();

        when(roleRepository.findByOrganizationIdAndName(orgId, roleName)).thenReturn(Optional.of(role));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(orgId, roleName)).thenReturn(5L);

        assertThatThrownBy(() -> roleService.deleteRole(orgId, roleName))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot delete role while it is assigned to members");

        verify(roleRepository, never()).delete(any());
    }
}
