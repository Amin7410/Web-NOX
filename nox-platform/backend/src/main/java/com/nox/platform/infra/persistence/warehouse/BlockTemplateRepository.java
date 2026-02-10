package com.nox.platform.infra.persistence.warehouse;

import com.nox.platform.core.warehouse.model.BlockTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockTemplateRepository extends JpaRepository<BlockTemplate, UUID> {
    Optional<BlockTemplate> findByNameAndWarehouseId(String name, UUID warehouseId);
}
