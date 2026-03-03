package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Project> findBySlugAndOrganizationId(String slug, UUID organizationId);

    boolean existsBySlugAndOrganizationId(String slug, UUID organizationId);

    Page<Project> findAllByOrganizationId(UUID organizationId, Pageable pageable);
}
