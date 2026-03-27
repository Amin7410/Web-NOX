package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CreateCoreBlockRequest;
import com.nox.platform.module.engine.api.request.UpdateCoreBlockRequest;
import com.nox.platform.module.engine.api.response.CoreBlockResponse;
import com.nox.platform.module.engine.service.CoreBlockService;
import com.nox.platform.module.iam.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/blocks")
@RequiredArgsConstructor
public class StudioBlockController {

    private final CoreBlockService coreBlockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoreBlockResponse createBlock(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateCoreBlockRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return coreBlockService.createBlock(workspaceId, request, userDetails.getId());
    }

    @GetMapping
    public List<CoreBlockResponse> getBlocks(@PathVariable UUID workspaceId) {
        return coreBlockService.getWorkspaceBlocks(workspaceId);
    }

    @PatchMapping("/{blockId}")
    public CoreBlockResponse updateBlock(
            @PathVariable UUID workspaceId,
            @PathVariable UUID blockId,
            @Valid @RequestBody UpdateCoreBlockRequest request) {
        return coreBlockService.updateBlock(workspaceId, blockId, request);
    }

    @DeleteMapping("/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlock(
            @PathVariable UUID workspaceId,
            @PathVariable UUID blockId) {
        coreBlockService.deleteBlock(workspaceId, blockId);
    }

    @PostMapping("/{blockId}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lockBlock(
            @PathVariable UUID workspaceId,
            @PathVariable UUID blockId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        coreBlockService.lockBlock(workspaceId, blockId, userDetails.getId());
    }

    @PostMapping("/{blockId}/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockBlock(
            @PathVariable UUID workspaceId,
            @PathVariable UUID blockId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        coreBlockService.unlockBlock(workspaceId, blockId, userDetails.getId());
    }
}
