package com.nox.platform.core.engine.service;

import com.nox.platform.api.dto.ProjectCreateRequest;
import com.nox.platform.core.engine.model.Project;
import com.nox.platform.core.organization.model.Organization;
import com.nox.platform.core.identity.model.User;
import com.nox.platform.infra.persistence.engine.ProjectRepository;
import com.nox.platform.infra.persistence.identity.UserRepository;
import com.nox.platform.infra.persistence.organization.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private ProjectCreateRequest createRequest;
    private Organization defaultOrg;
    private User defaultUser;

    @BeforeEach
    void setUp() {
        createRequest = new ProjectCreateRequest();
        createRequest.setName("Test Project");
        createRequest.setSlug("test-project");
        createRequest.setDescription("Description");

        defaultOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name("Default Org")
                .build();

        defaultUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();
    }

    @Test
    void createProject_Success_WithExistingOrgAndUser() {
        // Arrange
        createRequest.setCreatorId(defaultUser.getId());

        when(organizationRepository.findAll()).thenReturn(List.of(defaultOrg));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Project result = projectService.createProject(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createRequest.getName(), result.getName());
        assertEquals(defaultOrg, result.getOrganization());
        assertEquals(defaultUser.getId(), result.getCreatedById());
    }

    @Test
    void createProject_CreatesDefaultOrg_IfNoneExists() {
        // Arrange
        createRequest.setCreatorId(defaultUser.getId());

        when(organizationRepository.findAll()).thenReturn(Collections.emptyList());
        when(organizationRepository.save(any(Organization.class))).thenReturn(defaultOrg);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Project result = projectService.createProject(createRequest);

        // Assert
        assertNotNull(result);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void createProject_UsesFallbackUser_IfCreatorIdNull() {
        // Arrange
        createRequest.setCreatorId(null);

        when(organizationRepository.findAll()).thenReturn(List.of(defaultOrg));
        when(userRepository.findAll()).thenReturn(List.of(defaultUser));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            return p;
        });

        // Act
        Project result = projectService.createProject(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(defaultUser.getId(), result.getCreatedById());
    }
}
