package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateProjectRequest;
import com.nox.platform.module.engine.api.request.UpdateProjectRequest;
import com.nox.platform.module.engine.api.response.ProjectResponse;
import com.nox.platform.module.engine.service.ProjectService;
import com.nox.platform.shared.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request, SecurityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<Page<ProjectResponse>> getProjects(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjects(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/by-slug/{slug}")
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<ProjectResponse> getProjectBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(projectService.getProjectBySlug(slug));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
