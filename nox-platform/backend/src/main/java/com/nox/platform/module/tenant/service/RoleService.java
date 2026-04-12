package com.nox.platform.module.tenant.service;

import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.module.tenant.service.command.CreateRoleCommand;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.infrastructure.aspect.AuditTargetOrg;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public Role createRole(CreateRoleCommand command) {
        Organization org = organizationRepository.findById(command.orgId())
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));

        if (roleRepository.existsByOrganizationIdAndName(command.orgId(), command.name())) {
            throw new DomainException("ROLE_EXISTS", "Role name already exists in this organization", 400);
        }

        Role role = Role.create(org, command.name(), command.permissions(), command.level(), timeProvider.now());
        return roleRepository.save(role);
    }

    @Transactional
    public void provisionDefaultRoles(Organization organization) {
        OffsetDateTime now = timeProvider.now();
        roleRepository.save(Role.create(organization, "OWNER", List.of("*", "workspace:manage", "workspace:read"), 100, now));
        roleRepository.save(Role.create(organization, "ADMIN", List.of("iam:manage", "billing:manage", "workspace:manage"), 50, now));
        roleRepository.save(Role.create(organization, "MEMBER", List.of("workspace:read"), 10, now));
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
    public Role updatePermissions(UUID orgId, UUID roleId, List<String> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role not found", 404));

        if (!role.getOrganization().getId().equals(orgId)) {
            throw new DomainException("INVALID_ORG", "Role does not belong to this organization", 400);
        }

        role.updatePermissions(permissions);
        role.updateTimestamp(timeProvider.now());
        return roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = "org_members", allEntries = true)
    public void deleteRole(@AuditTargetOrg UUID orgId, String roleName) {
        Role role = roleRepository.findByOrganizationIdAndName(orgId, roleName)
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role not found", 404));

        long memberCount = orgMemberRepository.countByOrganizationIdAndRoleName(orgId, roleName);
        if (memberCount > 0) {
            throw new DomainException("ROLE_IN_USE", "Cannot delete role while it is assigned to members", 400);
        }

        OffsetDateTime now = timeProvider.now();
        role.softDelete(now);
        role.updateTimestamp(now);
        roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = "org_members", allEntries = true)
    public void softDeleteByOrgId(UUID orgId, OffsetDateTime now) {
        roleRepository.softDeleteByOrgId(orgId, now);
    }
}
