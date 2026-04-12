package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByProjectId(UUID projectId);

    Optional<Workspace> findByIdAndProjectId(UUID id, UUID projectId);

    @Modifying
    @Query("UPDATE Workspace w SET w.deletedAt = :deletedAt WHERE w.project.id = :projectId")
    void softDeleteByProjectId(@Param("projectId") UUID projectId, @Param("deletedAt") java.time.OffsetDateTime deletedAt);

    @Modifying
    @Query(value = """
            DELETE FROM workspaces 
            WHERE id IN (
                SELECT id FROM workspaces 
                WHERE deleted_at < :threshold 
                LIMIT :limit
            )
            """, nativeQuery = true)
    int deleteOldWorkspacesInBatch(@Param("threshold") java.time.OffsetDateTime threshold, @Param("limit") int limit);
}
