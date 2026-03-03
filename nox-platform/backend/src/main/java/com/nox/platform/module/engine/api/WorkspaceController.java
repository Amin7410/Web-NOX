package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateWorkspaceRequest;
import com.nox.platform.module.engine.api.response.WorkspaceResponse;
import com.nox.platform.module.engine.service.WorkspaceService;
import com.nox.platform.shared.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(projectId, request,
                SecurityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<List<WorkspaceResponse>> getWorkspaces(@PathVariable UUID projectId) {
        return ResponseEntity.ok(workspaceService.getWorkspacesByProject(projectId));
    }

    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable UUID projectId,
            @PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(projectId, workspaceId);
        return ResponseEntity.noContent().build();
    }
}
