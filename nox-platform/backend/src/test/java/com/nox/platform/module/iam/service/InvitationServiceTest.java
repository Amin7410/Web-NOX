package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.Invitation;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.InvitationRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvitationService invitationService;

    private User inviter;

    @BeforeEach
    void setUp() {
        inviter = User.builder()
                .id(UUID.randomUUID())
                .email("inviter@nox.com")
                .fullName("Inviter Admin")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void inviteUser_whenValid_savesAndEmails() {
        UUID orgId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(invitationRepository.existsByEmailAndOrgIdAndStatus("invitee@nox.com", orgId, "PENDING"))
                .thenReturn(false);

        invitationService.inviteUser("invitee@nox.com", orgId, roleId, inviter.getId());

        verify(invitationRepository).save(any(Invitation.class));
        verify(emailService).sendInvitationEmail(eq("invitee@nox.com"), anyString());
    }

    @Test
    void inviteUser_whenAlreadyPending_throwsException() {
        UUID orgId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(invitationRepository.existsByEmailAndOrgIdAndStatus("invitee@nox.com", orgId, "PENDING"))
                .thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> invitationService.inviteUser("invitee@nox.com", orgId, roleId, inviter.getId()));

        assertEquals("ALREADY_INVITED", ex.getCode());
        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendInvitationEmail(any(), any());
    }
}
