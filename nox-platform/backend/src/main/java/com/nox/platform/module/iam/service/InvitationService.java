package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.Invitation;
import com.nox.platform.module.iam.domain.InvitationStatus;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.InvitationRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public void inviteUser(String email, UUID orgId, UUID roleId, UUID inviterId) {
        if (invitationRepository.existsByEmailAndOrgIdAndStatus(email, orgId, InvitationStatus.PENDING)) {
            throw new DomainException("ALREADY_INVITED", "This Email has already been invited", 400);
        }

        String token = UUID.randomUUID().toString();

        OffsetDateTime now = timeProvider.now();
        Invitation invitation = Invitation.builder()
                .email(email)
                .orgId(orgId)
                .roleId(roleId)
                .invitedById(inviterId)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(now.plusDays(7))
                .build();
        invitation.initializeTimestamps(now);

        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(email, token);
    }

    @Transactional
    public void acceptInvitation(String token, UUID userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new DomainException("INVALID_INVITATION", "Invitation token is invalid", 400));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        invitation.accept(user, timeProvider.now());

        Organization org = organizationRepository.findById(invitation.getOrgId())
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization no longer exists", 404));

        Role role = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role no longer exists", 404));

        User inviter = userRepository.findById(invitation.getInvitedById()).orElse(null);

        if (orgMemberRepository.existsByOrganizationIdAndUserId(org.getId(), user.getId())) {
            invitationRepository.save(invitation);
            return; // Already a member
        }

        OffsetDateTime acceptTime = timeProvider.now();
        OrgMember member = OrgMember.builder()
                .organization(org)
                .user(user)
                .role(role)
                .invitedBy(inviter)
                .joinedAt(acceptTime)
                .build();
        member.initializeTimestamps(acceptTime);
        orgMemberRepository.save(member);

        invitationRepository.save(invitation);
    }
}
