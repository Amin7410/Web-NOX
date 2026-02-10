package com.nox.platform.core.engine.service;

import com.nox.platform.api.dto.ProjectCreateRequest;
import com.nox.platform.core.engine.model.Project;
import com.nox.platform.core.organization.model.Organization;
import com.nox.platform.core.identity.model.User;
import com.nox.platform.infra.persistence.engine.ProjectRepository;
import com.nox.platform.infra.persistence.organization.OrganizationRepository;
import com.nox.platform.infra.persistence.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Project createProject(ProjectCreateRequest request) {
        // Find or Create Default Organization
        Organization org = organizationRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Organization defaultOrg = Organization.builder()
                            .name("Default Org")
                            .slug("default-org")
                            .settings(Map.of())
                            .build();
                    return organizationRepository.save(defaultOrg);
                });

        // Resolve Creator
        UUID creatorId = request.getCreatorId();
        if (creatorId == null) {
            // Fallback to first user in DB
            creatorId = userRepository.findAll().stream().findFirst()
                    .map(User::getId)
                    .orElseThrow(() -> new RuntimeException("No users found in database. Create a user first!"));
        }

        Project project = Project.builder()
                .organization(org)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .createdById(creatorId)
                .build();

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}
