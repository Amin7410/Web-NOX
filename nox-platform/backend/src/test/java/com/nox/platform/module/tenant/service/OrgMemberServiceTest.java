package com.nox.platform.module.tenant.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrgMemberServiceTest {

    @Mock
    private OrgMemberRepository orgMemberRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrgMemberService orgMemberService;

    private Organization mockOrg;
    private User mockUser;
    private Role ownerRole;
    private OrgMember mockMember;

    @BeforeEach
    void setUp() {
        mockOrg = Organization.builder().name("Test Org").slug("test-org").build();
        mockOrg.setId(UUID.randomUUID());

        mockUser = User.builder().email("test@example.com").build();
        mockUser.setId(UUID.randomUUID());

        ownerRole = Role.builder().organization(mockOrg).name("OWNER").build();

        mockMember = OrgMember.builder()
                .organization(mockOrg)
                .user(mockUser)
                .role(ownerRole)
                .build();
    }

    @Test
    void removeMember_WhenUserIsLastOwner_ThrowsDomainException() {
        // Arrange
        when(orgMemberRepository.findByOrganizationIdAndUserId(mockOrg.getId(), mockUser.getId()))
                .thenReturn(Optional.of(mockMember));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(mockOrg.getId(), "OWNER"))
                .thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> orgMemberService.removeMember(mockOrg.getId(), mockUser.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot remove the last owner of an organization.");

        verify(orgMemberRepository, never()).delete(any());
    }

    @Test
    void removeMember_WhenUserIsOwnerButNotLast_RemovesSuccessfully() {
        // Arrange
        when(orgMemberRepository.findByOrganizationIdAndUserId(mockOrg.getId(), mockUser.getId()))
                .thenReturn(Optional.of(mockMember));
        when(orgMemberRepository.countByOrganizationIdAndRoleName(mockOrg.getId(), "OWNER"))
                .thenReturn(2L); // 2 owners

        // Act
        orgMemberService.removeMember(mockOrg.getId(), mockUser.getId());

        // Assert
        verify(orgMemberRepository).delete(mockMember);
    }
}
