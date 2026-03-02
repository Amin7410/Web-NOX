package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLDelete;
import java.time.OffsetDateTime;

@Entity
@Table(name = "asset_collections")
@SQLDelete(sql = "UPDATE asset_collections SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetCollection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_collection_id")
    private AssetCollection parentCollection;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public AssetCollection(Warehouse warehouse, String name, AssetCollection parentCollection) {
        this.warehouse = warehouse;
        this.name = name;
        this.parentCollection = parentCollection;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
