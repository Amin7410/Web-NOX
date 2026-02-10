package com.nox.platform.infra.persistence.engine;

import com.nox.platform.core.engine.model.CoreBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoreBlockRepository extends JpaRepository<CoreBlock, UUID> {
    List<CoreBlock> findByWorkspaceIdAndDeletedAtIsNull(UUID workspaceId);
}
