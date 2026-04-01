package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateCoreRelationRequest;
import com.nox.platform.module.engine.api.request.UpdateCoreRelationRequest;
import com.nox.platform.module.engine.api.response.CoreRelationResponse;
import com.nox.platform.module.engine.service.CoreRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/relations")
@RequiredArgsConstructor
public class StudioRelationController {

    private final CoreRelationService coreRelationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoreRelationResponse createRelation(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateCoreRelationRequest request) {
        return coreRelationService.createRelation(workspaceId, request);
    }

    @GetMapping
    public List<CoreRelationResponse> getRelations(@PathVariable UUID workspaceId) {
        return coreRelationService.getWorkspaceRelations(workspaceId);
    }

    @PatchMapping("/{relationId}")
    public CoreRelationResponse updateRelation(
            @PathVariable UUID workspaceId,
            @PathVariable UUID relationId,
            @Valid @RequestBody UpdateCoreRelationRequest request) {
        return coreRelationService.updateRelation(workspaceId, relationId, request);
    }

    @DeleteMapping("/{relationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRelation(
            @PathVariable UUID workspaceId,
            @PathVariable UUID relationId) {
        coreRelationService.deleteRelation(workspaceId, relationId);
    }
}
