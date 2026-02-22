package com.nox.platform.module.tenant.infrastructure;

import com.nox.platform.module.tenant.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByOrganizationId(UUID orgId);

    Optional<Role> findByOrganizationIdAndName(UUID orgId, String name);

    boolean existsByOrganizationIdAndName(UUID orgId, String name);
}
