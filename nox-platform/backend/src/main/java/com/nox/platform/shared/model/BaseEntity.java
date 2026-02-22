package com.nox.platform.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base class for all entities in the system.
 * Provides a common set of fields like ID, created_at, updated_at.
 * 
 * We use @MappedSuperclass to ensure these fields are inherited by
 * actual @Entity classes.
 * We use @SuperBuilder to allow the Builder pattern to construct both parent
 * and child fields.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // We intentionally don't add deleted_at here globally, because not EVERY
    // table uses soft delete (e.g. mapping tables like org_members might just hard
    // delete).
    // Subclasses that need soft delete will implement it themselves and use
    // @SQLRestriction.
}
