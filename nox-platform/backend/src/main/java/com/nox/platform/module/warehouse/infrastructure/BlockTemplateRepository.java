package com.nox.platform.module.warehouse.infrastructure;

import com.nox.platform.module.warehouse.domain.BlockTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlockTemplateRepository extends JpaRepository<BlockTemplate, UUID> {
    @EntityGraph(attributePaths = { "warehouse", "collection" })
    List<BlockTemplate> findByWarehouseId(UUID warehouseId);

    @EntityGraph(attributePaths = { "warehouse", "collection" })
    List<BlockTemplate> findByCollectionId(UUID collectionId);

    @Modifying
    @Query("UPDATE BlockTemplate b SET b.deletedAt = CURRENT_TIMESTAMP WHERE b.warehouse.id = :warehouseId")
    void softDeleteByWarehouseId(UUID warehouseId);
}
