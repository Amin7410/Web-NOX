package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.CoreSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoreSnapshotRepository extends JpaRepository<CoreSnapshot, UUID> {

    List<CoreSnapshot> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CoreSnapshot c SET c.deletedAt = :deletedAt WHERE c.project.id = :projectId AND c.deletedAt IS NULL")
    void softDeleteByProjectId(@Param("projectId") UUID projectId, @Param("deletedAt") java.time.OffsetDateTime deletedAt);

}
