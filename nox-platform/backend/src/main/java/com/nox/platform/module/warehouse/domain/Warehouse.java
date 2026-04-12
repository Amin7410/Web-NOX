package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
