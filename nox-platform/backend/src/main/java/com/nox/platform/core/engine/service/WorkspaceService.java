package com.nox.platform.core.engine.service;

import com.nox.platform.api.dto.BlockCreateRequest;
import com.nox.platform.api.dto.GraphResponse;
import com.nox.platform.core.engine.model.CoreBlock;
import com.nox.platform.core.engine.model.CoreRelation;
import com.nox.platform.core.engine.model.Workspace;
import com.nox.platform.core.warehouse.model.BlockTemplate;
import com.nox.platform.infra.persistence.engine.CoreBlockRepository;
import com.nox.platform.infra.persistence.engine.CoreRelationRepository;
import com.nox.platform.infra.persistence.engine.WorkspaceRepository;
import com.nox.platform.infra.persistence.warehouse.BlockTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final CoreBlockRepository coreBlockRepository;
    private final CoreRelationRepository coreRelationRepository;
    private final BlockTemplateRepository blockTemplateRepository;

    public GraphResponse getGraph(UUID workspaceId) {
        List<CoreBlock> blocks = coreBlockRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId);
        List<CoreRelation> relations = coreRelationRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId);
        return GraphResponse.builder()
                .nodes(blocks)
                .edges(relations)
                .build();
    }

    @Transactional
    public CoreBlock addBlock(UUID workspaceId, BlockCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        BlockTemplate template = blockTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Block Template not found"));

        CoreBlock block = CoreBlock.builder()
                .workspace(workspace)
                .originAsset(template)
                .name(template.getName()) // Inherit name initially
                .type("CANONICAL") // Default to canonical for now
                .visual(Map.of("x", request.getX(), "y", request.getY()))
                .config(template.getStructureData()) // Initial config from template structure
                .build();

        return coreBlockRepository.save(block);
    }
}
