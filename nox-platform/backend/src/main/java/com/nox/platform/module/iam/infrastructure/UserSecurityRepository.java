package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, UUID> {

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE UserSecurity s SET s.failedLoginAttempts = s.failedLoginAttempts + 1, s.updatedAt = :updatedAt WHERE s.user.id = :userId")
    void incrementFailedLogins(@org.springframework.data.repository.query.Param("userId") java.util.UUID userId, @org.springframework.data.repository.query.Param("updatedAt") java.time.OffsetDateTime updatedAt);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE UserSecurity s SET s.failedLoginAttempts = 0, s.lockedUntil = NULL, s.updatedAt = :updatedAt WHERE s.user.id = :userId")
    void resetFailedLogins(@org.springframework.data.repository.query.Param("userId") java.util.UUID userId, @org.springframework.data.repository.query.Param("updatedAt") java.time.OffsetDateTime updatedAt);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE UserSecurity s SET s.lockedUntil = :lockedUntil, s.updatedAt = :updatedAt WHERE s.user.id = :userId")
    void lockAccount(@org.springframework.data.repository.query.Param("userId") java.util.UUID userId,
            @org.springframework.data.repository.query.Param("lockedUntil") java.time.OffsetDateTime lockedUntil, @org.springframework.data.repository.query.Param("updatedAt") java.time.OffsetDateTime updatedAt);
}
