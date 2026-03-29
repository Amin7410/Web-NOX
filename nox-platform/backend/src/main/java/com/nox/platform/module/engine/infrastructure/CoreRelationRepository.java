package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.CoreRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface CoreRelationRepository extends JpaRepository<CoreRelation, UUID> {

    Optional<CoreRelation> findByIdAndWorkspace_Id(UUID id, UUID workspaceId);

    List<CoreRelation> findByWorkspaceIdOrderByCreatedAtAsc(UUID workspaceId);

    List<CoreRelation> findBySourceBlock_IdOrTargetBlock_Id(UUID sourceBlockId, UUID targetBlockId);

    @Modifying
    @Query("UPDATE CoreRelation r SET r.deletedAt = CURRENT_TIMESTAMP WHERE (r.sourceBlock.id IN :blockIds OR r.targetBlock.id IN :blockIds) AND r.deletedAt IS NULL")
    void softDeleteRelationsByBlockIds(@Param("blockIds") List<UUID> blockIds);
}
