package com.nox.platform.module.tenant.service.organization;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.module.tenant.service.RoleService;
import com.nox.platform.shared.abstraction.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService Unit Tests")
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private OrgMemberRepository orgMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrganizationService organizationService;

    private final String creatorEmail = "creator@example.com";
    private User creator;
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        creator = User.builder().id(UUID.randomUUID()).email(creatorEmail).build();
        lenient().when(timeProvider.now()).thenReturn(now);
    }

    @Nested
    @DisplayName("Creation Scenarios")
    class CreationTests {

        @Test
        @DisplayName("Should successfully create a new organization with default roles and owner")
        void shouldCreateOrganization() {
            // Given
            String orgName = "New Organization";
            when(userRepository.findByEmail(creatorEmail)).thenReturn(Optional.of(creator));
            when(organizationRepository.existsBySlug(anyString())).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));
            
            Role ownerRole = Role.builder().name("OWNER").level(100).build();
            when(roleService.createRole(any(), eq("OWNER"), any(), eq(100))).thenReturn(ownerRole);

            // When
            Organization result = organizationService.createOrganization(orgName, creatorEmail);

            // Then
            assertThat(result.getName()).isEqualTo(orgName);
            assertThat(result.getSlug()).isEqualTo("new-organization");
            
            verify(organizationRepository).save(any(Organization.class));
            verify(roleService, times(3)).createRole(any(), anyString(), any(), anyInt());
            verify(orgMemberRepository).save(any());
            verify(eventPublisher).publishEvent(any(com.nox.platform.shared.event.OrganizationCreatedEvent.class));
        }

        @Test
        @DisplayName("Should handle slug collisions by appending a hash")
        void shouldHandleSlugCollision() {
            // Given
            String orgName = "Hào Nam";
            when(userRepository.findByEmail(creatorEmail)).thenReturn(Optional.of(creator));
            
            // First attempt "hao-nam" exists, second one "hao-nam-xxxxxx" is free
            when(organizationRepository.existsBySlug("hao-nam")).thenReturn(true);
            when(organizationRepository.existsBySlug(argThat(s -> s.startsWith("hao-nam-")))).thenReturn(false);
            
            when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Organization result = organizationService.createOrganization(orgName, creatorEmail);

            // Then
            assertThat(result.getSlug()).startsWith("hao-nam-");
            assertThat(result.getSlug().length()).isEqualTo("hao-nam-".length() + 6);
        }
    }

    @Nested
    @DisplayName("Deletion Scenarios")
    class DeletionTests {

        @Test
        @DisplayName("Should soft delete org and its members and roles")
        void shouldSoftDeleteOrganization() {
            // Given
            UUID orgId = UUID.randomUUID();
            Organization org = Organization.builder().id(orgId).name("To Delete").build();
            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

            // When
            organizationService.deleteOrganization(orgId);

            // Then
            assertThat(org.getDeletedAt()).isEqualTo(now);
            verify(orgMemberRepository).softDeleteByOrgId(orgId, now);
            verify(roleRepository).softDeleteByOrgId(orgId, now);
            verify(eventPublisher).publishEvent(any(com.nox.platform.shared.event.OrganizationDeletedEvent.class));
        }
    }
}
