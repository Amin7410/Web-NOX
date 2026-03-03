package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateProjectRequest;
import com.nox.platform.module.engine.api.request.UpdateProjectRequest;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.ProjectStatus;
import com.nox.platform.module.engine.domain.ProjectVisibility;
import com.nox.platform.module.engine.infrastructure.ProjectRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.engine.infrastructure.CoreSnapshotRepository;
import com.nox.platform.module.engine.infrastructure.WorkspaceRepository;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.security.NoxUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private CoreSnapshotRepository snapshotRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID mockUserId;
    private UUID mockOrgId;
    private User mockUser;
    private Organization mockOrg;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockOrgId = UUID.randomUUID();

        mockUser = User.builder().id(mockUserId).email("test@nox.com").build();
        mockOrg = Organization.builder().slug("test-org").name("Test Org").build();
        ReflectionTestUtils.setField(mockOrg, "id", mockOrgId);

        NoxUserDetails mockUserDetails = mock(NoxUserDetails.class);
        lenient().when(mockUserDetails.getId()).thenReturn(mockUserId);
        lenient().when(mockUserDetails.getOrganizationId()).thenReturn(mockOrgId);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetails, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_success() {
        CreateProjectRequest request = new CreateProjectRequest("My App", "desc", ProjectVisibility.PRIVATE);

        when(organizationRepository.getReferenceById(mockOrgId)).thenReturn(mockOrg);
        when(userRepository.getReferenceById(mockUserId)).thenReturn(mockUser);
        when(projectRepository.existsBySlugAndOrganizationId("my-app", mockOrgId)).thenReturn(false);

        Project savedProject = Project.builder()
                .id(UUID.randomUUID())
                .name("My App")
                .slug("my-app")
                .createdBy(mockUser)
                .organization(mockOrg)
                .build();
        when(projectRepository.save(any())).thenReturn(savedProject);

        var result = projectService.createProject(request, mockUserId);

        assertEquals("My App", result.name());
        assertEquals("my-app", result.slug());
    }

    @Test
    void createProject_duplicateSlug_addsIncrement() {
        CreateProjectRequest request = new CreateProjectRequest("My App", "desc", null);

        when(organizationRepository.getReferenceById(mockOrgId)).thenReturn(mockOrg);
        when(userRepository.getReferenceById(mockUserId)).thenReturn(mockUser);

        when(projectRepository.existsBySlugAndOrganizationId("my-app", mockOrgId)).thenReturn(true);
        when(projectRepository.existsBySlugAndOrganizationId("my-app-1", mockOrgId)).thenReturn(false);

        Project savedProject = Project.builder().id(UUID.randomUUID()).name("My App").slug("my-app-1")
                .createdBy(mockUser).organization(mockOrg).build();
        when(projectRepository.save(any())).thenReturn(savedProject);

        var result = projectService.createProject(request, mockUserId);

        assertEquals("my-app-1", result.slug());
    }

    @Test
    void createProject_tenantRequired_throwsException() {
        SecurityContextHolder.clearContext(); // Remove Context

        CreateProjectRequest request = new CreateProjectRequest("My App", "desc", null);

        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.createProject(request, mockUserId));

        assertEquals("TENANT_REQUIRED", exception.getCode());
    }

    @Test
    void getProjects_success() {
        Project p1 = Project.builder().id(UUID.randomUUID()).name("P1").createdBy(mockUser).organization(mockOrg)
                .build();
        Project p2 = Project.builder().id(UUID.randomUUID()).name("P2").createdBy(mockUser).organization(mockOrg)
                .build();
        Page<Project> page = new PageImpl<>(List.of(p1, p2));

        when(projectRepository.findAllByOrganizationId(eq(mockOrgId), any())).thenReturn(page);

        var result = projectService.getProjects(PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
    }

    @Test
    void getProjects_noTenant_throwsException() {
        SecurityContextHolder.clearContext();

        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.getProjects(PageRequest.of(0, 10)));

        assertEquals("TENANT_REQUIRED", exception.getCode());
    }

    @Test
    void getProjectById_crossTenantAccess_throwsException() {
        UUID queryProjectId = UUID.randomUUID();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.getProjectById(queryProjectId));

        assertEquals("PROJECT_NOT_FOUND", exception.getCode());
    }

    @Test
    void getProjectById_success() {
        UUID queryProjectId = UUID.randomUUID();
        Project proj = Project.builder().id(queryProjectId).name("P1").createdBy(mockUser).organization(mockOrg)
                .build();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.of(proj));

        var result = projectService.getProjectById(queryProjectId);

        assertEquals(queryProjectId, result.id());
    }

    @Test
    void getProjectBySlug_success() {
        Project proj = Project.builder().id(UUID.randomUUID()).name("P1").slug("p1").createdBy(mockUser)
                .organization(mockOrg).build();
        when(projectRepository.findBySlugAndOrganizationId("p1", mockOrgId)).thenReturn(Optional.of(proj));

        var result = projectService.getProjectBySlug("p1");

        assertEquals("p1", result.slug());
    }

    @Test
    void getProjectBySlug_noTenant_throwsException() {
        SecurityContextHolder.clearContext();

        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.getProjectBySlug("p1"));

        assertEquals("TENANT_REQUIRED", exception.getCode());
    }

    @Test
    void updateProject_success() {
        UUID queryProjectId = UUID.randomUUID();
        Project existing = Project.builder().id(queryProjectId).name("Old Name").slug("old-name").createdBy(mockUser)
                .organization(mockOrg).build();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.of(existing));
        when(projectRepository.existsBySlugAndOrganizationId("new-name", mockOrgId)).thenReturn(false);
        when(projectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProjectRequest request = new UpdateProjectRequest("New Name", "New Desc", ProjectVisibility.PUBLIC,
                ProjectStatus.SUSPENDED);

        var result = projectService.updateProject(queryProjectId, request);

        assertEquals("New Name", result.name());
        assertEquals("new-name", result.slug());
        assertEquals("New Desc", result.description());
        assertEquals(ProjectVisibility.PUBLIC, result.visibility());
        assertEquals(ProjectStatus.SUSPENDED, result.status());
    }

    @Test
    void updateProject_partialFields_ignoresNulls() {
        UUID queryProjectId = UUID.randomUUID();
        Project existing = Project.builder().id(queryProjectId).name("Old Name").slug("old-name").description("Old")
                .visibility(ProjectVisibility.PRIVATE).status(ProjectStatus.ACTIVE).createdBy(mockUser)
                .organization(mockOrg).build();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProjectRequest request = new UpdateProjectRequest(null, null, null, null);

        var result = projectService.updateProject(queryProjectId, request);

        assertEquals("Old Name", result.name());
        assertEquals("old-name", result.slug());
        assertEquals("Old", result.description());
        assertEquals(ProjectVisibility.PRIVATE, result.visibility());
        assertEquals(ProjectStatus.ACTIVE, result.status());
    }

    @Test
    void updateProject_sameName_avoidsSlugRegeneration() {
        UUID queryProjectId = UUID.randomUUID();
        Project existing = Project.builder().id(queryProjectId).name("Old Name").slug("old-name").createdBy(mockUser)
                .organization(mockOrg).build();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProjectRequest request = new UpdateProjectRequest("Old Name", null, null, null);

        var result = projectService.updateProject(queryProjectId, request);

        assertEquals("old-name", result.slug());
        verify(projectRepository, never()).existsBySlugAndOrganizationId(anyString(), any(UUID.class));
    }

    @Test
    void deleteProject_success() {
        UUID queryProjectId = UUID.randomUUID();
        Project existing = Project.builder().id(queryProjectId).name("Old").createdBy(mockUser).organization(mockOrg)
                .build();
        when(projectRepository.findByIdAndOrganizationId(queryProjectId, mockOrgId)).thenReturn(Optional.of(existing));

        projectService.deleteProject(queryProjectId);

        verify(snapshotRepository, times(1)).softDeleteByProjectId(queryProjectId);
        verify(projectRepository, times(1)).delete(existing);
    }

    @Test
    void findProjectInternal_noTenant_throwsException() {
        SecurityContextHolder.clearContext();

        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.findProjectInternal(UUID.randomUUID()));

        assertEquals("TENANT_REQUIRED", exception.getCode());
    }
}
