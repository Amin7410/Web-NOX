package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.CoreBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

import java.util.Optional;

public interface CoreBlockRepository extends JpaRepository<CoreBlock, UUID> {

    Optional<CoreBlock> findByIdAndWorkspace_Id(UUID id, UUID workspaceId);

    @Query("SELECT b FROM CoreBlock b " +
           "LEFT JOIN FETCH b.parentBlock " +
           "LEFT JOIN FETCH b.originAsset " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.workspace.id = :workspaceId " +
           "ORDER BY b.createdAt ASC")
    List<CoreBlock> findBlocksWithDetailsByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    List<CoreBlock> findByWorkspaceId(UUID workspaceId);

    List<CoreBlock> findByWorkspaceIdAndParentBlockIsNull(UUID workspaceId);

    List<CoreBlock> findByParentBlock_Id(UUID parentBlockId);

    @Query(value = """
        WITH RECURSIVE descendant_blocks AS (
            SELECT id FROM core_blocks WHERE id = :rootId AND deleted_at IS NULL
            UNION ALL
            SELECT cb.id FROM core_blocks cb
            INNER JOIN descendant_blocks db ON cb.parent_block_id = db.id
            WHERE cb.deleted_at IS NULL
        )
        SELECT id FROM descendant_blocks;
        """, nativeQuery = true)
    List<UUID> findDescendantBlockIdsByRootId(@Param("rootId") UUID rootId);

    @Modifying
    @Query("UPDATE CoreBlock b SET b.deletedAt = :deletedAt WHERE b.id IN :blockIds AND b.deletedAt IS NULL")
    void softDeleteBlocksByIds(@Param("blockIds") List<UUID> blockIds, @Param("deletedAt") java.time.OffsetDateTime deletedAt);

    @Modifying
    @Query(value = """
            DELETE FROM core_blocks 
            WHERE id IN (
                SELECT cb.id FROM core_blocks cb
                INNER JOIN workspaces w ON cb.workspace_id = w.id
                WHERE w.deleted_at < :threshold
                LIMIT :limit
            )
            """, nativeQuery = true)
    int deleteOldBlocksInBatch(@Param("threshold") java.time.OffsetDateTime threshold, @Param("limit") int limit);
}
