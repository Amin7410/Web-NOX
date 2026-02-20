package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.UserMfaBackupCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserMfaBackupCodeRepository extends JpaRepository<UserMfaBackupCode, UUID> {
    Page<UserMfaBackupCode> findByUserIdAndUsedFalse(UUID userId, Pageable pageable);
}
