package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    boolean existsByEmailAndOrgIdAndStatus(String email, UUID orgId,
            com.nox.platform.module.iam.domain.InvitationStatus status);
}
