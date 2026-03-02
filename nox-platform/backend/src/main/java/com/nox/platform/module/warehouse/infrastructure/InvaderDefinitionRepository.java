package com.nox.platform.module.warehouse.infrastructure;

import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvaderDefinitionRepository extends JpaRepository<InvaderDefinition, UUID> {
    @EntityGraph(attributePaths = { "warehouse", "collection" })
    List<InvaderDefinition> findByWarehouseId(UUID warehouseId);

    @EntityGraph(attributePaths = { "warehouse", "collection" })
    List<InvaderDefinition> findByCollectionId(UUID collectionId);

    @EntityGraph(attributePaths = { "warehouse", "collection" })
    Optional<InvaderDefinition> findByWarehouseIdAndCode(UUID warehouseId, String code);

    @Modifying
    @Query("UPDATE InvaderDefinition i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.warehouse.id = :warehouseId")
    void softDeleteByWarehouseId(UUID warehouseId);
}
