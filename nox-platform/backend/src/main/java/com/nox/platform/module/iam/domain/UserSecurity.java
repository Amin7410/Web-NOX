package com.nox.platform.module.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_security")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Setter(AccessLevel.PUBLIC)
    private String passwordHash;

    @Column(name = "is_password_set", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private boolean isPasswordSet = false;

    @Column(name = "last_password_change")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime lastPasswordChange;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime lockedUntil;

    @Column(name = "failed_mfa_attempts", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private int failedMfaAttempts = 0;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret", columnDefinition = "TEXT")
    @Setter(AccessLevel.PROTECTED)
    private String mfaSecret;

    @Column(name = "temp_mfa_secret", columnDefinition = "TEXT")
    @Setter(AccessLevel.PROTECTED)
    private String tempMfaSecret;

    @Column(name = "updated_at", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime updatedAt;

    public void updateTimestamp(OffsetDateTime now) {
        this.updatedAt = now;
    }

    public void incrementFailedLogins(OffsetDateTime currentTime) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount(currentTime, 30); 
        }
    }

    public void resetFailedLogins() {
        this.failedLoginAttempts = 0;
        this.failedMfaAttempts = 0;
        this.lockedUntil = null;
    }

    public void incrementFailedMfaAttempts(OffsetDateTime currentTime) {
        this.failedMfaAttempts++;
        if (this.failedMfaAttempts >= 3) {
            lockAccount(currentTime, 60);
        }
    }

    public void lockAccount(OffsetDateTime baseTime, long minutes) {
        this.lockedUntil = baseTime.plusMinutes(minutes);
    }

    public boolean isLocked(OffsetDateTime currentTime) {
        return this.lockedUntil != null && this.lockedUntil.isAfter(currentTime);
    }

    public void initMfa(String tempSecret) {
        this.tempMfaSecret = tempSecret;
    }

    public void activateMfa(String secret) {
        this.mfaSecret = secret;
        this.mfaEnabled = true;
        this.tempMfaSecret = null;
        this.failedMfaAttempts = 0;
    }

    public void disableMfa() {
        this.mfaEnabled = false;
        this.mfaSecret = null;
        this.tempMfaSecret = null;
        this.failedMfaAttempts = 0;
    }

    public void completePasswordReset(String newPasswordHash, OffsetDateTime currentTime) {
        this.passwordHash = newPasswordHash;
        this.isPasswordSet = true;
        this.lastPasswordChange = currentTime;
        this.resetFailedLogins();
    }

    public void updatePassword(String newPasswordHash, OffsetDateTime currentTime) {
        this.passwordHash = newPasswordHash;
        this.lastPasswordChange = currentTime;
    }
}
