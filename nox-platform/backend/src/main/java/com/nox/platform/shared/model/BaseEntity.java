package com.nox.platform.shared.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    public void initializeTimestamps(OffsetDateTime now) {
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateTimestamp(OffsetDateTime now) {
        this.updatedAt = now;
    }

    // We intentionally don't add deleted_at here globally, because not EVERY
    // table uses soft delete (e.g. mapping tables like org_members might just hard
    // delete).
    // Subclasses that need soft delete will implement it themselves and use
    // @SQLRestriction.
}
