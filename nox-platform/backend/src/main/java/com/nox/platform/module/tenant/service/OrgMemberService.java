package com.nox.platform.module.tenant.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.service.command.AddMemberCommand;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nox.platform.shared.infrastructure.aspect.AuditTargetOrg;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgMemberService {

    private final OrgMemberRepository orgMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleService roleService;
    private final com.nox.platform.module.iam.service.InvitationService invitationService;
    private final UserRepository userRepository;
    private final com.nox.platform.shared.abstraction.TimeProvider timeProvider;

    @Transactional
    public OrgMember addMember(AddMemberCommand command) {
        Organization org = organizationRepository.findById(command.orgId())
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found to add to org", 404));

        User inviter = userRepository.findByEmail(command.inviterEmail())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Inviter tracking user not found", 404));

        if (orgMemberRepository.existsByOrganizationIdAndUserId(command.orgId(), user.getId())) {
            throw new DomainException("ALREADY_MEMBER", "User is already a member of this organization", 400);
        }

        Role role = roleService.getRoleByName(org.getId(), command.roleName());

        OrgMember inviterMember = orgMemberRepository.findByOrganizationIdAndUserId(command.orgId(), inviter.getId())
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Inviter must be a member of the organization", 403));

        if (!inviterMember.canAssignRole(role)) {
            throw new DomainException("INSUFFICIENT_PRIVILEGE",
                    "You cannot assign a role with a higher level than your own", 403);
        }

        OrgMember member = OrgMember.create(org, user, role, inviter, timeProvider.now());
        return orgMemberRepository.save(member);
    }

    @Transactional
    public void inviteMember(AddMemberCommand command) {
        Organization org = organizationRepository.findById(command.orgId())
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));

        Role role = roleService.getRoleByName(org.getId(), command.roleName());

        User inviter = userRepository.findByEmail(command.inviterEmail())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Inviter tracking user not found", 404));

        invitationService.inviteUser(command.email(), org.getId(), role.getId(), inviter.getId());
    }

    @Transactional
    public void provisionInitialOwner(Organization organization, User creator) {
        Role ownerRole = roleService.getRoleByName(organization.getId(), "OWNER");
        OrgMember member = OrgMember.create(organization, creator, ownerRole, creator, timeProvider.now());
        orgMemberRepository.save(member);
    }

    @Transactional
    public void softDeleteByOrgId(UUID orgId, OffsetDateTime now) {
        orgMemberRepository.softDeleteByOrgId(orgId, now);
    }

    public Page<OrgMember> getMembersByOrganization(UUID orgId, Pageable pageable) {
        return orgMemberRepository.findByOrganizationId(orgId, pageable);
    }

    public List<OrgMember> getOrganizationsForUser(UUID userId) {
        return orgMemberRepository.findByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = "org_members", key = "#orgId + '-' + #userId")
    public void removeMember(@AuditTargetOrg UUID orgId, UUID userId) {
        OrgMember member = orgMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new DomainException("MEMBER_NOT_FOUND", "User is not a member of this organization",
                        404));

        // Prevent removing the last owner
        if (member.getRole().getName().equals("OWNER")) {
            long ownerCount = orgMemberRepository.countByOrganizationIdAndRoleName(orgId, "OWNER");
            if (ownerCount <= 1) {
                throw new DomainException("CANNOT_REMOVE_LAST_OWNER",
                        "Cannot remove the last owner of an organization.", 400);
            }
        }

        OffsetDateTime now = timeProvider.now();
        member.softDelete(now);
        member.updateTimestamp(now);
        orgMemberRepository.save(member);
    }
}
