package com.nox.platform.module.tenant.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private OrgMemberRepository orgMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("creator@example.com")
                .fullName("Creator User")
                .build();
    }

    @Test
    void createOrganization_Success() {
        // Arrange
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(mockUser));
        when(organizationRepository.existsBySlug("my-company")).thenReturn(false);

        Organization savedOrg = Organization.builder()
                .name("My Company")
                .slug("my-company")
                .build();
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        Role ownerRole = Role.builder().name("OWNER").organization(savedOrg).build();
        when(roleService.createRole(any(), org.mockito.ArgumentMatchers.eq("OWNER"), any())).thenReturn(ownerRole);

        // Act
        Organization result = organizationService.createOrganization("My Company", "creator@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("my-company");
        verify(roleService, times(3)).createRole(any(), any(), any()); // OWNER, ADMIN, MEMBER
        verify(orgMemberRepository).save(any()); // Binds owner to OrgMember
    }

    @Test
    void createOrganization_SlugCollision_AppendsSuffix() {
        // Arrange
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(mockUser));
        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(true);
        when(organizationRepository.existsBySlug(org.mockito.ArgumentMatchers.startsWith("acme-corp-")))
                .thenReturn(true).thenReturn(false);

        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role ownerRole = Role.builder().name("OWNER").build();
        when(roleService.createRole(any(), org.mockito.ArgumentMatchers.eq("OWNER"), any())).thenReturn(ownerRole);

        // Act
        Organization result = organizationService.createOrganization("Acme Corp", "creator@example.com");

        // Assert
        assertThat(result.getSlug()).startsWith("acme-corp-");
        assertThat(result.getSlug().length()).isGreaterThan(9);
    }

    @Test
    void createOrganization_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> organizationService.createOrganization("Ghost Org", "ghost@example.com"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Creator user could not be found");
    }
}
