package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findByRefreshToken(String refreshToken);

    Page<UserSession> findByUserId(UUID userId, Pageable pageable);

    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = CURRENT_TIMESTAMP, s.revokeReason = :reason WHERE s.user.id = :userId AND s.revokedAt IS NULL AND s.expiresAt > CURRENT_TIMESTAMP")
    void revokeAllUserSessions(@Param("userId") UUID userId, @Param("reason") String reason);
}
