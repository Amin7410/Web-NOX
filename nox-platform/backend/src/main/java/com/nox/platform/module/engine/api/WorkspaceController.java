package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateWorkspaceRequest;
import com.nox.platform.module.engine.api.response.WorkspaceResponse;
import com.nox.platform.module.engine.service.WorkspaceService;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.exception.DomainException;
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
    private final SecurityProvider securityProvider;

    @PostMapping
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        
        UUID currentUserId = securityProvider.getCurrentUserId()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "User not authenticated"));

        WorkspaceResponse response = workspaceService.createWorkspace(projectId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getWorkspacesByProject(projectId)));
    }

    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable UUID projectId,
            @PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{workspaceId}/status")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspaceStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID workspaceId,
            @RequestParam com.nox.platform.module.engine.domain.WorkspaceStatus status) {
        WorkspaceResponse response = workspaceService.updateWorkspaceStatus(workspaceId, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
