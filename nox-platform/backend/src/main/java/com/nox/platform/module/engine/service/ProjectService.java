package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateProjectRequest;
import com.nox.platform.module.engine.api.request.UpdateProjectRequest;
import com.nox.platform.module.engine.api.response.ProjectResponse;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.ProjectVisibility;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.domain.WorkspaceType;
import com.nox.platform.module.engine.infrastructure.CoreSnapshotRepository;
import com.nox.platform.module.engine.infrastructure.ProjectRepository;
import com.nox.platform.module.engine.infrastructure.WorkspaceRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.abstraction.SlugGenerator;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CoreSnapshotRepository snapshotRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TimeProvider timeProvider;
    private final SecurityProvider securityProvider;
    private final SlugGenerator slugGenerator;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID currentUserId) {
        UUID orgId = request.organizationId();
        if (orgId == null) {
            throw new DomainException("TENANT_REQUIRED", "Organization context missing");
        }

        Organization org = organizationRepository.getReferenceById(orgId);
        User user = userRepository.getReferenceById(currentUserId);

        String generatedSlug = generateUniqueSlug(request.name(), orgId);

        OffsetDateTime now = timeProvider.now();
        Project project = Project.builder()
                .organization(org)
                .name(request.name())
                .slug(generatedSlug)
                .description(request.description())
                .visibility(request.visibility() != null ? request.visibility() : ProjectVisibility.PRIVATE)
                .createdBy(user)
                .build();
        project.initializeTimestamps(now);
        project = projectRepository.save(project);

        Workspace defaultWorkspace = Workspace.builder()
                .project(project)
                .name("Main Canvas")
                .type(WorkspaceType.MIXED)
                .createdBy(user)
                .build();
        defaultWorkspace.initializeTimestamps(now);
        workspaceRepository.save(defaultWorkspace);

        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(Pageable pageable, UUID orgId) {
        if (orgId == null) {
            orgId = securityProvider.getCurrentOrganizationId()
                    .orElseThrow(() -> new DomainException("TENANT_REQUIRED", "Organization context missing"));
        }
        return projectRepository.findAllByOrganizationId(orgId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        return toResponse(findProjectInternal(id));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectBySlug(String slug) {
        UUID orgId = securityProvider.getCurrentOrganizationId()
                .orElseThrow(() -> new DomainException("TENANT_REQUIRED", "Organization context missing"));
        Project project = projectRepository.findBySlugAndOrganizationId(slug, orgId)
                .orElseThrow(() -> new DomainException("PROJECT_NOT_FOUND", "Project not found or accessible"));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        Project project = findProjectInternal(id);

        String newSlug = project.getSlug();
        if (request.name() != null && !request.name().equals(project.getName())) {
            newSlug = generateUniqueSlug(request.name(), project.getOrganization().getId());
        }

        project.updateMetadata(request.name(), newSlug, request.description(), request.visibility(), request.status());
        project.updateTimestamp(timeProvider.now());
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProjectInternal(id);
        OffsetDateTime now = timeProvider.now();

        snapshotRepository.softDeleteByProjectId(project.getId(), now);
        workspaceRepository.softDeleteByProjectId(project.getId(), now);

        project.softDelete(now);
        project.updateTimestamp(now);
        projectRepository.save(project);
    }

    protected Project findProjectInternal(UUID id) {
        UUID orgId = securityProvider.getCurrentOrganizationId()
                .orElseThrow(() -> new DomainException("TENANT_REQUIRED", "Organization context missing"));
        return projectRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new DomainException("PROJECT_NOT_FOUND", "Project not found or accessible"));
    }

    private String generateUniqueSlug(String name, UUID orgId) {
        String baseSlug = slugGenerator.generate(name);
        String finalSlug = baseSlug;
        int counter = 1;

        while (projectRepository.existsBySlugAndOrganizationId(finalSlug, orgId)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
            if (counter > 20) {
                throw new DomainException("SLUG_GENERATION_FAILED", "Failed to generate a unique slug after 20 attempts.");
            }
        }
        return finalSlug;
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getVisibility(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}

