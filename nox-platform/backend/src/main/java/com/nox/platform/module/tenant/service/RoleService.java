package com.nox.platform.module.tenant.service;

import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nox.platform.shared.infrastructure.aspect.AuditTargetOrg;

import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrgMemberRepository orgMemberRepository;

    @Transactional
    public Role createRole(Organization organization, String name, List<String> permissions, Integer level) {
        if (roleRepository.existsByOrganizationIdAndName(organization.getId(), name)) {
            throw new DomainException("ROLE_EXISTS", "Role name already exists in this organization", 400);
        }

        int requestedLevel = level != null ? level : 0;

        java.util.UUID currentUserId = com.nox.platform.shared.util.SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            orgMemberRepository.findByOrganizationIdAndUserId(organization.getId(), currentUserId)
                    .ifPresent(member -> {
                        if (member.getRole().getLevel() < requestedLevel) {
                            throw new DomainException("INSUFFICIENT_PRIVILEGE",
                                    "You cannot create a role with a level higher than your own", 403);
                        }
                    });
        }

        Role role = Role.builder()
                .organization(organization)
                .name(name)
                .permissions(permissions)
                .level(level != null ? level : 0)
                .build();

        return roleRepository.save(role);
    }

    public List<Role> getRolesByOrganization(UUID orgId) {
        return roleRepository.findByOrganizationId(orgId);
    }

    public Role getRoleByName(UUID orgId, String name) {
        return roleRepository.findByOrganizationIdAndName(orgId, name)
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role not found in this organization", 404));
    }

    @Transactional
    @CacheEvict(value = "org_members", allEntries = true)
    public Role updatePermissions(UUID roleId, List<String> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role not found", 404));

        if ("OWNER".equalsIgnoreCase(role.getName())) {
            throw new DomainException("IMMUTABLE_ROLE", "Cannot modify permissions of the OWNER role", 400);
        }

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = "org_members", allEntries = true)
    public void deleteRole(@AuditTargetOrg UUID orgId, String roleName) {
        Role role = roleRepository.findByOrganizationIdAndName(orgId, roleName)
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role not found", 404));

        if ("OWNER".equalsIgnoreCase(role.getName())) {
            throw new DomainException("IMMUTABLE_ROLE", "The OWNER role cannot be deleted", 400);
        }

        long memberCount = orgMemberRepository.countByOrganizationIdAndRoleName(orgId, roleName);
        if (memberCount > 0) {
            throw new DomainException("ROLE_IN_USE", "Cannot delete role while it is assigned to members", 400);
        }

        roleRepository.delete(role);
    }
}
