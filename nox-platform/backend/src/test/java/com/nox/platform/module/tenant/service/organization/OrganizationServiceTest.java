package com.nox.platform.module.tenant.service.organization;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.module.tenant.service.RoleService;
import com.nox.platform.module.tenant.service.OrgMemberService;
import com.nox.platform.module.tenant.service.command.CreateOrganizationCommand;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.util.SlugGenerator;
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
    private OrgMemberService orgMemberService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SlugGenerator slugGenerator;

    @InjectMocks
    private OrganizationService organizationService;

    private final String creatorEmail = "creator@example.com";
    private User creator;
    private final OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        creator = User.builder().id(UUID.randomUUID()).email(creatorEmail).build();
        lenient().when(timeProvider.now()).thenReturn(now);
        lenient().when(slugGenerator.generate(anyString())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            return input.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
        });
    }

    @Nested
    @DisplayName("Creation Scenarios")
    class CreationTests {

        @Test
        @DisplayName("Should successfully create a new organization with default roles and owner")
        void shouldCreateOrganization() {
            // Given
            String orgName = "New Organization";
            CreateOrganizationCommand command = new CreateOrganizationCommand(orgName, creatorEmail);
            
            when(userRepository.findByEmail(creatorEmail)).thenReturn(Optional.of(creator));
            when(organizationRepository.existsBySlug(anyString())).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Organization result = organizationService.createOrganization(command);

            // Then
            assertThat(result.getName()).isEqualTo(orgName);
            assertThat(result.getSlug()).isEqualTo("new-organization");
            
            verify(organizationRepository).save(any(Organization.class));
            verify(roleService).provisionDefaultRoles(any(Organization.class));
            verify(orgMemberService).provisionInitialOwner(any(Organization.class), eq(creator));
            verify(eventPublisher).publishEvent(any(com.nox.platform.shared.event.OrganizationCreatedEvent.class));
        }

        @Test
        @DisplayName("Should handle slug collisions by appending a hash")
        void shouldHandleSlugCollision() {
            // Given
            String orgName = "Hào Nam";
            CreateOrganizationCommand command = new CreateOrganizationCommand(orgName, creatorEmail);
            
            when(userRepository.findByEmail(creatorEmail)).thenReturn(Optional.of(creator));
            
            when(organizationRepository.existsBySlug("hao-nam")).thenReturn(true);
            when(organizationRepository.existsBySlug(argThat(s -> s.startsWith("hao-nam-")))).thenReturn(false);
            
            when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Organization result = organizationService.createOrganization(command);

            // Then
            assertThat(result.getSlug()).startsWith("hao-nam-");
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
            assertThat(org.getDeletedAt()).isNotNull();
            verify(orgMemberService).softDeleteByOrgId(orgId, now);
            verify(roleService).softDeleteByOrgId(orgId, now);
            verify(eventPublisher).publishEvent(any(com.nox.platform.shared.event.OrganizationDeletedEvent.class));
        }
    }
}
