package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import com.nox.platform.shared.exception.DomainException;
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
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Warehouse extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    @Setter(AccessLevel.PROTECTED)
    private OwnerType ownerType;

    @Column(name = "name")
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(name = "is_system", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private boolean isSystem;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;


    public static Warehouse create(UUID ownerId, OwnerType ownerType, String name, boolean isSystem, OffsetDateTime now) {
        Warehouse warehouse = Warehouse.builder()
                .ownerId(ownerId)
                .ownerType(ownerType)
                .name(name)
                .isSystem(isSystem)
                .build();
        warehouse.initializeTimestamps(now);
        return warehouse;
    }

    public void updateMetadata(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void markAsDeleted(OffsetDateTime currentTime) {
        if (this.isSystem) {
            throw new DomainException("SYSTEM_WAREHOUSE",
                "System warehouse deletion denied", 400);
        }
        this.deletedAt = currentTime;
    }
}
