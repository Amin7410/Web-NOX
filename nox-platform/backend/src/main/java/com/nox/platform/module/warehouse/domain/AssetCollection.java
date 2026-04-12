package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "asset_collections")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AssetCollection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Warehouse warehouse;

    @Column(name = "name", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_collection_id")
    @Setter(AccessLevel.PROTECTED)
    private AssetCollection parentCollection;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public static AssetCollection create(Warehouse warehouse, AssetCollection parent, String name, OffsetDateTime now) {
        AssetCollection collection = AssetCollection.builder()
                .warehouse(warehouse)
                .name(name)
                .parentCollection(parent)
                .build();
        collection.initializeTimestamps(now);
        return collection;
    }

    public void changeParent(AssetCollection newParent) {
        this.parentCollection = newParent;
    }

    public void markAsDeleted(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
