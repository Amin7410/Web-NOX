package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLDelete;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
@SQLDelete(sql = "UPDATE warehouses SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private OwnerType ownerType;

    @Column(name = "name")
    private String name;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public Warehouse(UUID ownerId, OwnerType ownerType, String name, boolean isSystem) {
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.name = name;
        this.isSystem = isSystem;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
