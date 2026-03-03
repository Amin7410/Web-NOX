package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateWorkspaceRequest;
import com.nox.platform.module.engine.api.response.WorkspaceResponse;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.infrastructure.WorkspaceRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(UUID projectId, CreateWorkspaceRequest request, UUID currentUserId) {
        // Enforces IDOR check inherently since findProjectInternal enforces Tenant
        // context natively
        Project project = projectService.findProjectInternal(projectId);

        User user = userRepository.getReferenceById(currentUserId);

        Workspace workspace = Workspace.builder()
                .project(project)
                .name(request.name())
                .type(request.type())
                .createdBy(user)
                .build();

        workspace = workspaceRepository.save(workspace);
        return mapToResponse(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByProject(UUID projectId) {
        // Enforces safety bounds matching
        projectService.findProjectInternal(projectId);

        List<Workspace> workspaces = workspaceRepository.findByProjectId(projectId);
        return workspaces.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteWorkspace(UUID projectId, UUID workspaceId) {
        // Validate Project Access first natively
        projectService.findProjectInternal(projectId);

        Workspace workspace = workspaceRepository.findByIdAndProjectId(workspaceId, projectId)
                .orElseThrow(
                        () -> new DomainException("WORKSPACE_NOT_FOUND", "Workspace missing or invalid bounds", 404));

        workspaceRepository.delete(workspace);
    }

    private WorkspaceResponse mapToResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getProject().getId(),
                workspace.getName(),
                workspace.getType(),
                workspace.getCreatedBy().getId(),
                workspace.getCreatedAt());
    }
}
