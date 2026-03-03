package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateSnapshotRequest;
import com.nox.platform.module.engine.api.response.SnapshotResponse;
import com.nox.platform.module.engine.service.EngineSnapshotService;
import com.nox.platform.shared.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/snapshots")
@RequiredArgsConstructor
public class SnapshotController {

    private final EngineSnapshotService engineSnapshotService;

    @PostMapping("/commit")
    @PreAuthorize("hasAuthority('workspace:manage')")
    public ResponseEntity<SnapshotResponse> commitDesignState(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateSnapshotRequest request) {
        SnapshotResponse response = engineSnapshotService.saveDesignSnapshot(projectId, request,
                SecurityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<List<SnapshotResponse>> getProjectSnapshots(@PathVariable UUID projectId) {
        return ResponseEntity.ok(engineSnapshotService.getProjectSnapshots(projectId));
    }

    @GetMapping("/{snapshotId}/payload")
    @PreAuthorize("hasAuthority('workspace:read')")
    public ResponseEntity<JsonNode> getSnapshotPayload(
            @PathVariable UUID projectId,
            @PathVariable UUID snapshotId) {
        return ResponseEntity.ok(engineSnapshotService.getSnapshotPayload(projectId, snapshotId));
    }
}
