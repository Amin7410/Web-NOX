package com.nox.platform.infra.persistence.engine;

import com.nox.platform.core.engine.model.CoreRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoreRelationRepository extends JpaRepository<CoreRelation, UUID> {
    List<CoreRelation> findByWorkspaceIdAndDeletedAtIsNull(UUID workspaceId);
}
