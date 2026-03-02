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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "assets_block_templates")
@SQLDelete(sql = "UPDATE assets_block_templates SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private AssetCollection collection;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structure_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> structureData = Map.of();

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public BlockTemplate(Warehouse warehouse, AssetCollection collection, String name, String description,
            String thumbnailUrl, Map<String, Object> structureData, String version) {
        this.warehouse = warehouse;
        this.collection = collection;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.structureData = structureData != null ? structureData : Map.of();
        this.version = version != null ? version : "1.0.0";
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
