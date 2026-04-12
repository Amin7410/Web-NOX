package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "assets_block_templates")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@AttributeOverride(name = "version", column = @Column(name = "version_lock"))
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
    @Builder.Default
    private Map<String, Object> structureData = Map.of();

    @Column(name = "version", length = 20)
    @Builder.Default
    private String templateVersion = "1.0.0";

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
