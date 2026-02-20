package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findByCodeAndType(String code, OtpCode.OtpType type);

    List<OtpCode> findByUser_IdAndTypeAndUsedAtIsNull(UUID userId, OtpCode.OtpType type);
}
