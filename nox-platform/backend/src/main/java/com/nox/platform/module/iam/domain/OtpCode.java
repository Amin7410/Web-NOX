package com.nox.platform.module.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_codes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PROTECTED)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User user;

    @Column(nullable = false, length = 6)
    @Setter(AccessLevel.PROTECTED)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.PROTECTED)
    private OtpType type;

    @Column(name = "expires_at", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime usedAt;

    @Default
    @Column(name = "failed_attempts", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private int failedAttempts = 0;

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

    public enum OtpType {
        VERIFY_EMAIL,
        RESET_PASSWORD
    }

    public boolean isExpired(OffsetDateTime currentTime) {
        return currentTime.isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isValid(OffsetDateTime currentTime) {
        return !isExpired(currentTime) && !isUsed();
    }

    public void markAsUsed(OffsetDateTime currentTime) {
        this.usedAt = currentTime;
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
}
