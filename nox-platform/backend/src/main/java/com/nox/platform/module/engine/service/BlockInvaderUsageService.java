package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.AttachInvaderRequest;
import com.nox.platform.module.engine.api.response.BlockInvaderUsageResponse;
import com.nox.platform.module.engine.domain.BlockInvaderUsage;
import com.nox.platform.module.engine.domain.CoreBlock;
import com.nox.platform.module.engine.infrastructure.BlockInvaderUsageRepository;
import com.nox.platform.module.engine.infrastructure.CoreBlockRepository;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockInvaderUsageService {

    private final BlockInvaderUsageRepository usageRepository;
    private final CoreBlockRepository blockRepository;
    private final InvaderDefinitionRepository invaderRepository;
    private final WorkspaceService workspaceService;

    @Transactional
    public BlockInvaderUsageResponse attachInvader(UUID blockId, AttachInvaderRequest request) {
        CoreBlock block = blockRepository.findById(blockId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        workspaceService.getWorkspaceInternal(block.getWorkspace().getId());

        InvaderDefinition invader = invaderRepository.findById(request.invaderAssetId())
                .orElseThrow(() -> new DomainException("INVADER_NOT_FOUND", "Invader definition not found", 404));

        // Prevent duplicate attach
        if (usageRepository.findByBlock_IdAndInvaderAsset_Id(blockId, request.invaderAssetId()).isPresent()) {
            throw new DomainException("INVADER_ALREADY_ATTACHED", "Invader already attached to this block", 400);
        }

        BlockInvaderUsage usage = BlockInvaderUsage.builder()
                .block(block)
                .invaderAsset(invader)
                .appliedVersion(request.appliedVersion())
                .configSnapshot(request.configSnapshot())
                .build();

        usage = usageRepository.save(usage);
        return mapToResponse(usage);
    }

    @Transactional
    public void detachInvader(UUID usageId) {
        BlockInvaderUsage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new DomainException("USAGE_NOT_FOUND", "Invader usage not found", 404));

        workspaceService.getWorkspaceInternal(usage.getBlock().getWorkspace().getId());

        usageRepository.delete(usage);
    }

    @Transactional
    public void deleteUsagesForBlocks(List<UUID> blockIds) {
        if (blockIds != null && !blockIds.isEmpty()) {
            usageRepository.softDeleteUsagesByBlockIds(blockIds);
        }
    }

    @Transactional(readOnly = true)
    public List<BlockInvaderUsageResponse> getBlockInvaders(UUID blockId) {
        CoreBlock block = blockRepository.findById(blockId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        workspaceService.getWorkspaceInternal(block.getWorkspace().getId());

        return usageRepository.findByBlock_IdOrderByCreatedAtAsc(blockId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BlockInvaderUsageResponse mapToResponse(BlockInvaderUsage usage) {
        return new BlockInvaderUsageResponse(
                usage.getId(),
                usage.getBlock().getId(), // NOPMD
                usage.getInvaderAsset().getId(), // NOPMD
                usage.getAppliedVersion(),
                usage.getConfigSnapshot(),
                usage.getCreatedAt());
    }
}
