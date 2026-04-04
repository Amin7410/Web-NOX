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
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CoreSnapshotRepository snapshotRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID currentUserId) {
        UUID orgId = request.organizationId();
        if (orgId == null) {
            throw new DomainException("TENANT_REQUIRED", "Organization context missing", 400);
        }

        Organization org = organizationRepository.getReferenceById(orgId);
        User user = userRepository.getReferenceById(currentUserId);

        String generatedSlug = generateSlug(request.name(), orgId);

        Project project = Project.builder()
                .organization(org)
                .name(request.name())
                .slug(generatedSlug)
                .description(request.description())
                .visibility(request.visibility() != null ? request.visibility() : ProjectVisibility.PRIVATE)
                .createdBy(user)
                .build();

        project = projectRepository.save(project);

        Workspace defaultWorkspace = Workspace.builder()
                .project(project)
                .name("Main Canvas")
                .type(WorkspaceType.MIXED)
                .createdBy(user)
                .build();
        workspaceRepository.save(defaultWorkspace);

        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(Pageable pageable, UUID orgId) {
        if (orgId == null) {
            orgId = SecurityUtil.getCurrentOrganizationId();
        }
        
        if (orgId == null) {
            // Find first org user belongs to if no context
            List<Organization> userOrgs = organizationRepository.findAll(); // Simple fallback for now
            if (!userOrgs.isEmpty()) {
                orgId = userOrgs.get(0).getId();
            }
        }

        if (orgId == null) {
            throw new DomainException("TENANT_REQUIRED", "Organization context missing", 400);
        }
        return projectRepository.findAllByOrganizationId(orgId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        return mapToResponse(findProjectInternal(id));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectBySlug(String slug) {
        UUID orgId = SecurityUtil.getCurrentOrganizationId();
        if (orgId == null) {
            throw new DomainException("TENANT_REQUIRED", "Organization context missing", 400);
        }
        Project project = projectRepository.findBySlugAndOrganizationId(slug, orgId)
                .orElseThrow(() -> new DomainException("PROJECT_NOT_FOUND", "Project not found or accessible", 404));
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        Project project = findProjectInternal(id);

        if (request.name() != null && !request.name().equals(project.getName())) {
            project.setName(request.name());
            project.setSlug(generateSlug(request.name(), project.getOrganization().getId()));
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }

        if (request.visibility() != null) {
            project.setVisibility(request.visibility());
        }

        if (request.status() != null) {
            project.setStatus(request.status());
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProjectInternal(id);
        snapshotRepository.softDeleteByProjectId(project.getId());
        projectRepository.delete(project);
    }

    protected Project findProjectInternal(UUID id) {
        UUID orgId = SecurityUtil.getCurrentOrganizationId();
        if (orgId == null) {
            throw new DomainException("TENANT_REQUIRED", "Organization context missing", 400);
        }
        return projectRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new DomainException("PROJECT_NOT_FOUND", "Project not found or accessible", 404));
    }

    private String generateSlug(String name, UUID orgId) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String baseSlug = normalized.toLowerCase().replaceAll("[^a-z0-9\\-]", "-").replaceAll("-+", "-");
        baseSlug = baseSlug.replaceAll("^-+|-+$", "");

        String finalSlug = baseSlug;
        int counter = 1;

        while (projectRepository.existsBySlugAndOrganizationId(finalSlug, orgId)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }
        return finalSlug;
    }

    private ProjectResponse mapToResponse(Project project) {
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
