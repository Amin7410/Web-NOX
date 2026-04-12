package com.nox.platform.module.engine.service.mapper;

import com.nox.platform.module.engine.api.response.CoreRelationResponse;
import com.nox.platform.module.engine.domain.CoreRelation;
import com.nox.platform.shared.mapping.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class CoreRelationMapper implements BaseMapper<CoreRelation, CoreRelationResponse> {

    @Override
    public CoreRelationResponse toResponse(CoreRelation relation) {
        if (relation == null) return null;
        
        return new CoreRelationResponse(
                relation.getId(),
                relation.getWorkspace().getId(),
                relation.getSourceBlock().getId(),
                relation.getTargetBlock().getId(),
                relation.getType(),
                relation.getRules(),
                relation.getVisual(),
                relation.getDeletedAt());
    }
}
