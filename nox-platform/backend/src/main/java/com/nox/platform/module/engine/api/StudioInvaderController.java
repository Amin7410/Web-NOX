package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.AttachInvaderRequest;
import com.nox.platform.module.engine.api.response.BlockInvaderUsageResponse;
import com.nox.platform.module.engine.service.BlockInvaderUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blocks/{blockId}/invaders")
@RequiredArgsConstructor
public class StudioInvaderController {

    private final BlockInvaderUsageService invaderUsageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlockInvaderUsageResponse attachInvader(
            @PathVariable UUID blockId,
            @Valid @RequestBody AttachInvaderRequest request) {
        return invaderUsageService.attachInvader(blockId, request);
    }

    @GetMapping
    public List<BlockInvaderUsageResponse> getBlockInvaders(@PathVariable UUID blockId) {
        return invaderUsageService.getBlockInvaders(blockId);
    }

    @DeleteMapping("/{usageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachInvader(
            @PathVariable UUID blockId,
            @PathVariable UUID usageId) {
        invaderUsageService.detachInvader(usageId);
    }
}
