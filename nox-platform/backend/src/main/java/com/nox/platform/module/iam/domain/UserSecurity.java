package com.nox.platform.module.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain entity representing the security credentials and settings of a User.
 * Shared PK with User table.
 */
@Entity
@Table(name = "user_security")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "is_password_set", nullable = false)
    @Builder.Default
    private boolean isPasswordSet = false;

    @Column(name = "last_password_change")
    private OffsetDateTime lastPasswordChange;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret", columnDefinition = "TEXT")
    private String mfaSecret;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // --- Domain Behaviors ---

    public void incrementFailedLogins() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLogins() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void lockAccount(long minutes) {
        this.lockedUntil = OffsetDateTime.now().plusMinutes(minutes);
    }

    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(OffsetDateTime.now());
    }
}
