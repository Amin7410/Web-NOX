package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.BlockInvaderUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BlockInvaderUsageRepository extends JpaRepository<BlockInvaderUsage, UUID> {

    List<BlockInvaderUsage> findByBlock_IdOrderByCreatedAtAsc(UUID blockId);

    Optional<BlockInvaderUsage> findByBlock_IdAndInvaderAsset_Id(UUID blockId, UUID invaderAssetId);

    @Modifying
    @Query("UPDATE BlockInvaderUsage u SET u.deletedAt = :deletedAt WHERE u.block.id IN :blockIds AND u.deletedAt IS NULL")
    void softDeleteUsagesByBlockIds(@Param("blockIds") List<UUID> blockIds, @Param("deletedAt") java.time.OffsetDateTime deletedAt);
}
