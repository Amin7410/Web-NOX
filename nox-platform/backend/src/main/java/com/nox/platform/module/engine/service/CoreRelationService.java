package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateCoreRelationRequest;
import com.nox.platform.module.engine.api.request.UpdateCoreRelationRequest;
import com.nox.platform.module.engine.api.response.CoreRelationResponse;
import com.nox.platform.module.engine.domain.CoreBlock;
import com.nox.platform.module.engine.domain.CoreRelation;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.infrastructure.CoreBlockRepository;
import com.nox.platform.module.engine.infrastructure.CoreRelationRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreRelationService {

    private final CoreRelationRepository coreRelationRepository;
    private final CoreBlockRepository coreBlockRepository;
    private final WorkspaceService workspaceService;

    @Transactional
    public CoreRelationResponse createRelation(UUID workspaceId, CreateCoreRelationRequest request) {
        Workspace workspace = workspaceService.getWorkspaceInternal(workspaceId);

        CoreBlock sourceBlock = coreBlockRepository.findById(request.sourceBlockId())
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Source block not found", 404));

        CoreBlock targetBlock = coreBlockRepository.findById(request.targetBlockId())
                .orElseThrow(() -> new DomainException("BLOCK_NOT_FOUND", "Target block not found", 404));

        if (!sourceBlock.getWorkspace().getId().equals(workspaceId) || 
            !targetBlock.getWorkspace().getId().equals(workspaceId)) {
            throw new DomainException("INVALID_WORKSPACE", "Both blocks must be in the same current workspace..", 403);
        }

        // Enforce unique index idx_core_relations_source_target
        boolean exists = coreRelationRepository.findBySourceBlock_IdOrTargetBlock_Id(
                request.sourceBlockId(), request.targetBlockId()).stream()
                .anyMatch(r -> r.getSourceBlock().getId().equals(request.sourceBlockId())
                            && r.getTargetBlock().getId().equals(request.targetBlockId()));
        if (exists) {
            throw new DomainException("RELATION_EXISTS", "Relation between these blocks already exists", 400);
        }

        CoreRelation relation = CoreRelation.builder()
                .workspace(workspace)
                .sourceBlock(sourceBlock)
                .targetBlock(targetBlock)
                .type(request.type())
                .rules(request.rules() != null ? request.rules() : Map.of())
                .visual(request.visual() != null ? request.visual() : Map.of())
                .build();

        relation = coreRelationRepository.save(relation);
        return mapToResponse(relation);
    }

    @Transactional
    public CoreRelationResponse updateRelation(UUID workspaceId, UUID relationId, UpdateCoreRelationRequest request) {
        workspaceService.getWorkspaceInternal(workspaceId);

        CoreRelation relation = coreRelationRepository.findByIdAndWorkspace_Id(relationId, workspaceId)
                .orElseThrow(() -> new DomainException("RELATION_NOT_FOUND", "Relation not found in this workspace", 404));

        if (request.rules() != null) {
            relation.setRules(request.rules()); // NOPMD
        }

        if (request.visual() != null) {
            relation.setVisual(request.visual()); // NOPMD
        }

        relation = coreRelationRepository.save(relation);
        return mapToResponse(relation);
    }

    @Transactional
    public void deleteRelation(UUID workspaceId, UUID relationId) {
        workspaceService.getWorkspaceInternal(workspaceId);

        CoreRelation relation = coreRelationRepository.findByIdAndWorkspace_Id(relationId, workspaceId)
                .orElseThrow(() -> new DomainException("RELATION_NOT_FOUND", "Relation not found in this workspace", 404));

        coreRelationRepository.delete(relation);
    }

    @Transactional
    public void deleteRelationsForBlocks(List<UUID> blockIds) {
        if (blockIds != null && !blockIds.isEmpty()) {
            coreRelationRepository.softDeleteRelationsByBlockIds(blockIds);
        }
    }

    @Transactional(readOnly = true)
    public List<CoreRelationResponse> getWorkspaceRelations(UUID workspaceId) {
        workspaceService.getWorkspaceInternal(workspaceId);

        return coreRelationRepository.findByWorkspaceIdOrderByCreatedAtAsc(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CoreRelationResponse mapToResponse(CoreRelation relation) {
        return new CoreRelationResponse(
                relation.getId(),
                relation.getWorkspace().getId(), // NOPMD
                relation.getSourceBlock().getId(), // NOPMD
                relation.getTargetBlock().getId(), // NOPMD
                relation.getType(),
                relation.getRules(),
                relation.getVisual(),
                relation.getDeletedAt()
        );
    }
}
