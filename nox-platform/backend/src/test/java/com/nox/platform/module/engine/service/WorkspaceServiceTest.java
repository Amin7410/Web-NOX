package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateWorkspaceRequest;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.domain.WorkspaceType;
import com.nox.platform.module.engine.infrastructure.WorkspaceRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private UUID mockProjectId;
    private UUID mockUserId;
    private User mockUser;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProjectId = UUID.randomUUID();
        mockUserId = UUID.randomUUID();
        mockUser = User.builder().id(mockUserId).build();
        mockProject = Project.builder().id(mockProjectId).build();
    }

    @Test
    void createWorkspace_delegatesProjectValidation_success() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("Backend Logic", WorkspaceType.BACKEND);

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);
        when(userRepository.getReferenceById(mockUserId)).thenReturn(mockUser);

        Workspace savedWs = Workspace.builder().id(UUID.randomUUID()).name("Backend Logic").type(WorkspaceType.BACKEND)
                .project(mockProject).createdBy(mockUser).build();
        when(workspaceRepository.save(any())).thenReturn(savedWs);

        var result = workspaceService.createWorkspace(mockProjectId, request, mockUserId);

        assertEquals("Backend Logic", result.name());
        assertEquals(WorkspaceType.BACKEND, result.type());
    }

    @Test
    void getWorkspacesByProject_success() {
        Workspace w1 = Workspace.builder().id(UUID.randomUUID()).name("W1").project(mockProject).createdBy(mockUser)
                .build();
        Workspace w2 = Workspace.builder().id(UUID.randomUUID()).name("W2").project(mockProject).createdBy(mockUser)
                .build();

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);
        when(workspaceRepository.findByProjectId(mockProjectId)).thenReturn(List.of(w1, w2));

        var results = workspaceService.getWorkspacesByProject(mockProjectId);

        assertEquals(2, results.size());
    }

    @Test
    void deleteWorkspace_success() {
        UUID wsId = UUID.randomUUID();
        Workspace w1 = Workspace.builder().id(wsId).name("W1").project(mockProject).createdBy(mockUser).build();

        when(workspaceRepository.findById(wsId)).thenReturn(Optional.of(w1));
        when(projectService.findProjectInternal(mockProject.getId())).thenReturn(mockProject);

        workspaceService.deleteWorkspace(wsId);

        verify(workspaceRepository, times(1)).delete(w1);
    }

    @Test
    void deleteWorkspace_notFound_throwsException() {
        UUID wsId = UUID.randomUUID();
        when(workspaceRepository.findById(wsId)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> workspaceService.deleteWorkspace(wsId));

        assertEquals("WORKSPACE_NOT_FOUND", ex.getCode());
    }
}
