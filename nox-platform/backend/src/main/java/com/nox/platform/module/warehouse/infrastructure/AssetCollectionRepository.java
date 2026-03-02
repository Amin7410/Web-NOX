package com.nox.platform.module.warehouse.infrastructure;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetCollectionRepository extends JpaRepository<AssetCollection, UUID> {
    @EntityGraph(attributePaths = { "warehouse", "parentCollection" })
    List<AssetCollection> findByWarehouseId(UUID warehouseId);

    @EntityGraph(attributePaths = { "warehouse", "parentCollection" })
    List<AssetCollection> findByWarehouseIdAndParentCollectionId(UUID warehouseId, UUID parentCollectionId);

    @EntityGraph(attributePaths = { "warehouse", "parentCollection" })
    List<AssetCollection> findByWarehouseIdAndParentCollectionIsNull(UUID warehouseId);

    @EntityGraph(attributePaths = { "warehouse", "parentCollection" })
    Optional<AssetCollection> findByWarehouseIdAndName(UUID warehouseId, String name);
}
