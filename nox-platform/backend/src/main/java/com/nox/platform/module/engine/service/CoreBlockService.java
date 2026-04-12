package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateCoreBlockRequest;
import com.nox.platform.module.engine.api.request.UpdateCoreBlockRequest;
import com.nox.platform.module.engine.api.response.CoreBlockResponse;
import com.nox.platform.module.engine.domain.CoreBlock;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.infrastructure.CoreBlockRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.module.iam.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nox.platform.shared.abstraction.TimeProvider;
import java.time.OffsetDateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreBlockService {

    private final CoreBlockRepository coreBlockRepository;
    private final WorkspaceService workspaceService;
    private final BlockTemplateRepository blockTemplateRepository;
    private final UserRepository userRepository;
    
    @Lazy
    private final CoreRelationService coreRelationService;
    @Lazy
    private final BlockInvaderUsageService blockInvaderUsageService;
    private final TimeProvider timeProvider;

    @Transactional
    public CoreBlockResponse createBlock(UUID workspaceId, CreateCoreBlockRequest request, UUID currentUserId) {
        Workspace workspace = workspaceService.getWorkspaceInternal(workspaceId);
        User user = userRepository.getReferenceById(currentUserId);

        CoreBlock parentBlock = null;
        if (request.parentBlockId() != null) {
            parentBlock = coreBlockRepository.findByIdAndWorkspace_Id(request.parentBlockId(), workspaceId)
                    .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Parent block not found in this workspace", 404));
        }

        BlockTemplate originAsset = null;
        if (request.originAssetId() != null) {
            originAsset = blockTemplateRepository.findById(request.originAssetId())
                    .orElseThrow(() -> new DomainException("ASSET_NOT_FOUND", "Origin asset not found", 404));
        }

        OffsetDateTime now = timeProvider.now();
        CoreBlock block = CoreBlock.builder()
                .id(request.id())
                .workspace(workspace)
                .parentBlock(null)
                .originAsset(originAsset)
                .type(request.type())
                .name(request.name())
                .config(request.config() != null ? request.config() : Map.of())
                .visual(request.visual() != null ? request.visual() : Map.of())
                .createdBy(user)
                .build();
        block.initializeTimestamps(now);

        if (parentBlock != null) {
            block.moveTo(parentBlock);
        }

        block = coreBlockRepository.save(block);
        return mapToResponse(block);
    }

    @Transactional
    public CoreBlockResponse updateBlock(UUID workspaceId, UUID blockId, UpdateCoreBlockRequest request) {
        workspaceService.getWorkspaceInternal(workspaceId);
        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found in this workspace", 404));

        UUID currentUserId = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails currentUser) {
            currentUserId = currentUser.getId();
        }

        block.updateContent(request.name(), request.config(), request.visual(), currentUserId, timeProvider.now());

        if (request.parentBlockId() != null) {
            CoreBlock parentBlock = coreBlockRepository.findByIdAndWorkspace_Id(request.parentBlockId(), workspaceId)
                    .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Parent block not found in this workspace", 404));
            
            block.moveTo(parentBlock);
        }

        block = coreBlockRepository.save(block);
        return mapToResponse(block);
    }

    @Transactional
    public void lockBlock(UUID workspaceId, UUID blockId, UUID userId) {
        workspaceService.getWorkspaceInternal(workspaceId);
        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        block.lock(userId, timeProvider.now());
        coreBlockRepository.save(block);
    }

    @Transactional
    public void unlockBlock(UUID workspaceId, UUID blockId, UUID userId) {
        workspaceService.getWorkspaceInternal(workspaceId);
        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        block.unlock(userId, timeProvider.now());
        coreBlockRepository.save(block);
    }

    @Transactional
    public void deleteBlock(UUID workspaceId, UUID blockId) {
        workspaceService.getWorkspaceInternal(workspaceId);

        coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found in this workspace", 404));

        List<UUID> descendantBlockIds = coreBlockRepository.findDescendantBlockIdsByRootId(blockId);

        if (descendantBlockIds != null && !descendantBlockIds.isEmpty()) {
            OffsetDateTime now = timeProvider.now();
            coreBlockRepository.softDeleteBlocksByIds(descendantBlockIds, now);
            coreRelationService.deleteRelationsForBlocks(descendantBlockIds, now);
            blockInvaderUsageService.deleteUsagesForBlocks(descendantBlockIds, now);
        }
    }

    @Transactional(readOnly = true)
    public List<CoreBlockResponse> getWorkspaceBlocks(UUID workspaceId) {
        workspaceService.getWorkspaceInternal(workspaceId);

        return coreBlockRepository.findBlocksWithDetailsByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CoreBlockResponse mapToResponse(CoreBlock block) {
        return new CoreBlockResponse(
                block.getId(),
                block.getWorkspace().getId(),
                block.getParentBlock() != null ? block.getParentBlock().getId() : null,
                block.getOriginAsset() != null ? block.getOriginAsset().getId() : null,
                block.getType(),
                block.getName(),
                block.getConfig(),
                block.getVisual(),
                block.getCreatedBy() != null ? block.getCreatedBy().getId() : null,
                block.getUpdatedAt(),
                block.getDeletedAt()
        );
    }
}
