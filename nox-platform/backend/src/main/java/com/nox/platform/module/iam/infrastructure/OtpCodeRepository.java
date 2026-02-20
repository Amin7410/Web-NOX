package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findByCodeAndType(String code, OtpCode.OtpType type);

    Optional<OtpCode> findFirstByUser_IdAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId, OtpCode.OtpType type);

    List<OtpCode> findByUser_IdAndTypeAndUsedAtIsNull(UUID userId, OtpCode.OtpType type);

    @Modifying
    @Query("UPDATE OtpCode o SET o.usedAt = CURRENT_TIMESTAMP WHERE o.user.id = :userId AND o.type = :type AND o.usedAt IS NULL")
    void invalidatePreviousOtps(@Param("userId") UUID userId, @Param("type") OtpCode.OtpType type);
}
