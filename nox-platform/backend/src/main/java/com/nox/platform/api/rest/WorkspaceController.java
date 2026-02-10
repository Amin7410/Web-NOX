package com.nox.platform.api.rest;

import com.nox.platform.api.dto.BlockCreateRequest;
import com.nox.platform.api.dto.GraphResponse;
import com.nox.platform.core.engine.model.CoreBlock;
import com.nox.platform.core.engine.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/{workspaceId}/graph")
    public ResponseEntity<GraphResponse> getGraph(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(workspaceService.getGraph(workspaceId));
    }

    @PostMapping("/{workspaceId}/blocks")
    public ResponseEntity<CoreBlock> addBlock(@PathVariable UUID workspaceId, @RequestBody BlockCreateRequest request) {
        log.info("Adding block to workspace {}: {}", workspaceId, request);
        return ResponseEntity.ok(workspaceService.addBlock(workspaceId, request));
    }
}
