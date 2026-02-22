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

    @Transactional
    public void inviteUser(String email, UUID orgId, UUID roleId, UUID inviterId) {
        if (invitationRepository.existsByEmailAndOrgIdAndStatus(email, orgId,
                com.nox.platform.module.iam.domain.InvitationStatus.PENDING)) {
            throw new DomainException("ALREADY_INVITED", "This Email has already been invited", 400);
        }

        String token = UUID.randomUUID().toString();

        Invitation invitation = Invitation.builder()
                .email(email)
                .orgId(orgId)
                .roleId(roleId)
                .invitedById(inviterId)
                .token(token)
                .status(com.nox.platform.module.iam.domain.InvitationStatus.PENDING)
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();

        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(email, token);
    }

    @Transactional
    public void acceptInvitation(String token, UUID userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new DomainException("INVALID_INVITATION", "Invitation token is invalid", 400));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DomainException("INVITATION_HANDLED", "This invitation is no longer pending", 400);
        }
        if (invitation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new DomainException("EXPIRED_INVITATION", "This invitation has expired", 400);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new DomainException("EMAIL_MISMATCH", "This invitation was sent to a different email address", 403);
        }

        Organization org = organizationRepository.findById(invitation.getOrgId())
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization no longer exists", 404));

        Role role = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> new DomainException("ROLE_NOT_FOUND", "Role no longer exists", 404));

        User inviter = userRepository.findById(invitation.getInvitedById()).orElse(null);

        if (orgMemberRepository.existsByOrganizationIdAndUserId(org.getId(), user.getId())) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(OffsetDateTime.now());
            invitationRepository.save(invitation);
            return; // Already a member
        }

        OrgMember member = OrgMember.builder()
                .organization(org)
                .user(user)
                .role(role)
                .invitedBy(inviter)
                .build();
        orgMemberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(OffsetDateTime.now());
        invitationRepository.save(invitation);
    }
}
