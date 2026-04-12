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

    List<CoreRelation> findBySourceBlock_IdAndTargetBlock_Id(UUID sourceBlockId, UUID targetBlockId);

    @Query("SELECT r FROM CoreRelation r WHERE (r.sourceBlock.id IN :blockIds OR r.targetBlock.id IN :blockIds) AND r.deletedAt IS NULL")
    List<CoreRelation> findByBlockIdsActive(@Param("blockIds") List<UUID> blockIds);

    @Modifying
    @Query("UPDATE CoreRelation r SET r.deletedAt = :deletedAt WHERE (r.sourceBlock.id IN :blockIds OR r.targetBlock.id IN :blockIds) AND r.deletedAt IS NULL")
    void softDeleteRelationsByBlockIds(@Param("blockIds") List<UUID> blockIds, @Param("deletedAt") java.time.OffsetDateTime deletedAt);

    @Modifying
    @Query(value = """
            DELETE FROM core_relations 
            WHERE id IN (
                SELECT cr.id FROM core_relations cr
                INNER JOIN workspaces w ON cr.workspace_id = w.id
                WHERE w.deleted_at < :threshold
                LIMIT :limit
            )
            """, nativeQuery = true)
    int deleteOldRelationsInBatch(@Param("threshold") java.time.OffsetDateTime threshold, @Param("limit") int limit);
}
