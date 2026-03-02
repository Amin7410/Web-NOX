package com.nox.platform.module.warehouse.infrastructure;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByOwnerIdAndOwnerType(UUID ownerId, OwnerType ownerType);

    List<Warehouse> findByOwnerId(UUID ownerId);
}
