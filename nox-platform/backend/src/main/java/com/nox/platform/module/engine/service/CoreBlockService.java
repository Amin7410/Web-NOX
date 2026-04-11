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
    
    // Injecting via Lazy to prevent any potential unforeseen bean cycle, though architecturally safe here
    @Lazy
    private final CoreRelationService coreRelationService;
    @Lazy
    private final BlockInvaderUsageService blockInvaderUsageService;

    @Transactional
    public CoreBlockResponse createBlock(UUID workspaceId, CreateCoreBlockRequest request, UUID currentUserId) {
        Workspace workspace = workspaceService.getWorkspaceInternal(workspaceId);

        User user = userRepository.getReferenceById(currentUserId);

        CoreBlock parentBlock = null;
        if (request.parentBlockId() != null) {
            parentBlock = coreBlockRepository.findByIdAndWorkspace_Id(request.parentBlockId(), workspaceId)
                    .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Parent block not found in this workspace", 404));

            validateParentAssignment(null, parentBlock);
        }

        BlockTemplate originAsset = null;
        if (request.originAssetId() != null) {
            originAsset = blockTemplateRepository.findById(request.originAssetId())
                    .orElseThrow(() -> new DomainException("ASSET_NOT_FOUND", "Origin asset not found", 404));
        }

        CoreBlock block = CoreBlock.builder()
                .id(request.id())
                .workspace(workspace)
                .parentBlock(parentBlock)
                .originAsset(originAsset)
                .type(request.type())
                .name(request.name())
                .config(request.config() != null ? request.config() : Map.of())
                .visual(request.visual() != null ? request.visual() : Map.of())
                .createdBy(user)
                .build();

        block = coreBlockRepository.save(block);
        return mapToResponse(block);
    }

    @Transactional
    public CoreBlockResponse updateBlock(UUID workspaceId, UUID blockId, UpdateCoreBlockRequest request) {
        workspaceService.getWorkspaceInternal(workspaceId);

        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found in this workspace", 404));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails currentUser) {
            if (block.getLockedBy() != null && !block.getLockedBy().equals(currentUser.getId())) {
                // Give 2 minutes grace period for stale locks
                if (block.getLockedAt().plusMinutes(2).isAfter(OffsetDateTime.now())) {
                    throw new DomainException("BLOCK_LOCKED", "Block is currently locked by another user", 423);
                }
            }
        }

        if (request.name() != null) {
            block.setName(request.name());
        }

        if (request.parentBlockId() != null) {
            CoreBlock parentBlock = coreBlockRepository.findByIdAndWorkspace_Id(request.parentBlockId(), workspaceId)
                    .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Parent block not found in this workspace", 404));
            
            validateParentAssignment(block.getId(), parentBlock);
            block.setParentBlock(parentBlock);
        }

        if (request.config() != null) {
            block.setConfig(request.config()); // NOPMD
        }

        if (request.visual() != null) {
            block.setVisual(request.visual()); // NOPMD
        }

        block = coreBlockRepository.save(block);
        return mapToResponse(block);
    }

    @Transactional
    public void lockBlock(UUID workspaceId, UUID blockId, UUID userId) {
        workspaceService.getWorkspaceInternal(workspaceId);
        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        if (block.getLockedBy() != null && !block.getLockedBy().equals(userId)) {
            if (block.getLockedAt().plusMinutes(2).isAfter(OffsetDateTime.now())) {
                throw new DomainException("BLOCK_LOCKED", "Block is already locked by another user", 423);
            }
        }
        block.setLockedBy(userId);
        block.setLockedAt(OffsetDateTime.now());
        coreBlockRepository.save(block);
    }

    @Transactional
    public void unlockBlock(UUID workspaceId, UUID blockId, UUID userId) {
        workspaceService.getWorkspaceInternal(workspaceId);
        CoreBlock block = coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found", 404));

        if (userId.equals(block.getLockedBy())) {
            block.setLockedBy(null);
            block.setLockedAt(null);
            coreBlockRepository.save(block);
        }
    }

    private void validateParentAssignment(UUID currentBlockId, CoreBlock targetParent) {
        int depth = 1;
        CoreBlock current = targetParent;
        while (current != null) {
            if (depth > 10) {
                throw new DomainException("MAX_DEPTH_REACHED", "Cannot nest blocks deeper than 10 levels.", 400);
            }
            if (currentBlockId != null && current.getId().equals(currentBlockId)) {
                throw new DomainException("CIRCULAR_DEPENDENCY", "Circular dependency detected: a block cannot be moved inside its own descendant.", 400);
            }
            // Fetch parent object if lazy initialization requires it, but in our case, parentBlock is already an entity.
            current = current.getParentBlock();
            depth++;
        }
    }

    @Transactional
    public void deleteBlock(UUID workspaceId, UUID blockId) {
        workspaceService.getWorkspaceInternal(workspaceId);

        coreBlockRepository.findByIdAndWorkspace_Id(blockId, workspaceId)
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Block not found in this workspace", 404));

        List<UUID> descendantBlockIds = coreBlockRepository.findDescendantBlockIdsByRootId(blockId);
        
        if (descendantBlockIds != null && !descendantBlockIds.isEmpty()) {
            coreBlockRepository.softDeleteBlocksByIds(descendantBlockIds);
            coreRelationService.deleteRelationsForBlocks(descendantBlockIds);
            blockInvaderUsageService.deleteUsagesForBlocks(descendantBlockIds);
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
                block.getWorkspace().getId(), // NOPMD
                block.getParentBlock() != null ? block.getParentBlock().getId() : null, // NOPMD
                block.getOriginAsset() != null ? block.getOriginAsset().getId() : null, // NOPMD
                block.getType(),
                block.getName(),
                block.getConfig(),
                block.getVisual(),
                block.getCreatedBy() != null ? block.getCreatedBy().getId() : null, // NOPMD
                block.getUpdatedAt(),
                block.getDeletedAt()
        );
    }
}
