package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.Invitation;
import com.nox.platform.module.iam.infrastructure.InvitationRepository;
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

    @Transactional
    public void inviteUser(String email, UUID orgId, UUID roleId, UUID inviterId) {
        if (invitationRepository.existsByEmailAndOrgIdAndStatus(email, orgId, "PENDING")) {
            throw new DomainException("ALREADY_INVITED", "This Email has already been invited", 400);
        }

        String token = UUID.randomUUID().toString();

        Invitation invitation = Invitation.builder()
                .email(email)
                .orgId(orgId)
                .roleId(roleId)
                .invitedById(inviterId)
                .token(token)
                .status("PENDING")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();

        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(email, token);
    }
}
