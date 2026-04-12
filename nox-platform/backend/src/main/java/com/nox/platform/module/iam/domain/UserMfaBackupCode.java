package com.nox.platform.module.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_mfa_backup_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMfaBackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime updatedAt;

    public void initializeTimestamps(OffsetDateTime now) {
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateTimestamp(OffsetDateTime now) {
        this.updatedAt = now;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    public void markAsUsed(OffsetDateTime currentTime) {
        this.used = true;
        this.usedAt = currentTime;
    }
}
