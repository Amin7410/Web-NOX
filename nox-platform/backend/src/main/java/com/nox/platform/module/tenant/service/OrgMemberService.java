package com.nox.platform.module.tenant.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgMemberService {

    private final OrgMemberRepository orgMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleService roleService;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "org_members", key = "#orgId + '-' + #userRepository.findByEmail(#email).map(u -> u.getId()).orElse(null)")
    public OrgMember addMember(UUID orgId, String email, String roleName, String inviterEmail) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found to add to org", 404));

        User inviter = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Inviter tracking user not found", 404));

        if (orgMemberRepository.existsByOrganizationIdAndUserId(orgId, user.getId())) {
            throw new DomainException("ALREADY_MEMBER", "User is already a member of this organization", 400);
        }

        Role role = roleService.getRoleByName(org.getId(), roleName);

        // Role Hierarchy Check: Inviter must have a strictly higher level than the role
        // being assigned
        OrgMember inviterMember = orgMemberRepository.findByOrganizationIdAndUserId(orgId, inviter.getId())
                .orElse(null);

        if (inviterMember == null) {
            // If they have '*' permission but aren't in the org, we might allow it?
            // In most cases, inviter should be in the org.
            throw new DomainException("UNAUTHORIZED", "Inviter must be a member of the organization", 403);
        }

        if (inviterMember.getRole().getLevel() <= role.getLevel()) {
            throw new DomainException("INSUFFICIENT_PRIVILEGE",
                    "You cannot assign a role with a level equal to or higher than your own", 403);
        }

        OrgMember member = OrgMember.builder()
                .organization(org)
                .user(user)
                .role(role)
                .invitedBy(inviter)
                .build();

        return orgMemberRepository.save(member);
    }

    public Page<OrgMember> getMembersByOrganization(UUID orgId, Pageable pageable) {
        return orgMemberRepository.findByOrganizationId(orgId, pageable);
    }

    public List<OrgMember> getOrganizationsForUser(UUID userId) {
        return orgMemberRepository.findByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = "org_members", key = "#orgId + '-' + #userId")
    public void removeMember(UUID orgId, UUID userId) {
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

        orgMemberRepository.delete(member);
    }
}
