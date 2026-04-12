package com.nox.platform.module.tenant.service.member;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.service.OrgMemberService;
import com.nox.platform.module.tenant.service.RoleService;
import com.nox.platform.module.tenant.service.command.AddMemberCommand;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrgMemberService Unit Tests")
class OrgMemberServiceTest {

    @Mock
    private OrgMemberRepository orgMemberRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private OrgMemberService orgMemberService;

    private final UUID orgId = UUID.randomUUID();
    private final String userEmail = "newbie@example.com";
    private final String inviterEmail = "admin@example.com";
    private final OffsetDateTime now = OffsetDateTime.now();
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder().id(orgId).build();
        lenient().when(timeProvider.now()).thenReturn(now);
    }

    @Nested
    @DisplayName("Adding Members Scenarios")
    class AddMemberTests {

        @Test
        @DisplayName("Should successfully add a member when inviter has high enough role level")
        void shouldAddMember() {
            // Given
            User user = User.builder().id(UUID.randomUUID()).email(userEmail).build();
            User inviter = User.builder().id(UUID.randomUUID()).email(inviterEmail).build();
            AddMemberCommand command = new AddMemberCommand(orgId, userEmail, "MEMBER", inviterEmail);
            
            Role adminRole = Role.builder().name("ADMIN").level(50).build();
            Role memberRole = Role.builder().name("MEMBER").level(10).build();
            OrgMember inviterMember = OrgMember.builder().role(adminRole).build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
            when(roleService.getRoleByName(orgId, "MEMBER")).thenReturn(memberRole);
            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, inviter.getId())).thenReturn(Optional.of(inviterMember));

            // When
            orgMemberService.addMember(command);

            // Then
            verify(orgMemberRepository).save(any(OrgMember.class));
        }

        @Test
        @DisplayName("Should throw exception if inviter role level is lower than target role")
        void shouldThrowOnHierarchyViolation() {
            // Given
            User user = User.builder().id(UUID.randomUUID()).email(userEmail).build();
            User inviter = User.builder().id(UUID.randomUUID()).email(inviterEmail).build();
            AddMemberCommand command = new AddMemberCommand(orgId, userEmail, "ADMIN", inviterEmail);
            
            Role moderatorRole = Role.builder().name("MODERATOR").level(30).build();
            Role adminRole = Role.builder().name("ADMIN").level(50).build();
            OrgMember inviterMember = OrgMember.builder().role(moderatorRole).build();

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail(inviterEmail)).thenReturn(Optional.of(inviter));
            when(roleService.getRoleByName(orgId, "ADMIN")).thenReturn(adminRole);
            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, inviter.getId())).thenReturn(Optional.of(inviterMember));

            // When & Then
            assertThatThrownBy(() -> orgMemberService.addMember(command))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "INSUFFICIENT_PRIVILEGE");
        }
    }

    @Nested
    @DisplayName("Removing Members Scenarios")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should prevent removing the last OWNER of an organization")
        void shouldPreventRemovingLastOwner() {
            // Given
            UUID userId = UUID.randomUUID();
            Role ownerRole = Role.builder().name("OWNER").build();
            OrgMember owner = OrgMember.builder().role(ownerRole).build();

            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, userId)).thenReturn(Optional.of(owner));
            when(orgMemberRepository.countByOrganizationIdAndRoleName(orgId, "OWNER")).thenReturn(1L);

            // When & Then
            assertThatThrownBy(() -> orgMemberService.removeMember(orgId, userId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "CANNOT_REMOVE_LAST_OWNER");
        }

        @Test
        @DisplayName("Should allow removing an owner if others exist")
        void shouldAllowRemovingOwnerIfNotEmpty() {
            // Given
            UUID userId = UUID.randomUUID();
            Role ownerRole = Role.builder().name("OWNER").build();
            OrgMember owner = OrgMember.builder().role(ownerRole).build();

            when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, userId)).thenReturn(Optional.of(owner));
            when(orgMemberRepository.countByOrganizationIdAndRoleName(orgId, "OWNER")).thenReturn(2L);

            // When
            orgMemberService.removeMember(orgId, userId);

            // Then
            assertThat(owner.getDeletedAt()).isEqualTo(now);
            verify(orgMemberRepository).save(owner);
        }
    }
}
