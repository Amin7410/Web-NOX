package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.module.engine.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByProjectId(UUID projectId);

    Optional<Workspace> findByIdAndProjectId(UUID id, UUID projectId);
}
