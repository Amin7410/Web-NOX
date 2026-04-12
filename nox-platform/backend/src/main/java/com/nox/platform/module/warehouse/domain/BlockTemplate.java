package com.nox.platform.module.warehouse.domain;

import com.nox.platform.module.warehouse.service.command.CreateBlockTemplateCommand;
import com.nox.platform.module.warehouse.service.command.UpdateBlockTemplateCommand;
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
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@AttributeOverride(name = "version", column = @Column(name = "version_lock"))
public class BlockTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    @Setter(AccessLevel.PROTECTED)
    private AssetCollection collection;

    @Column(name = "name", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Setter(AccessLevel.PROTECTED)
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    @Setter(AccessLevel.PROTECTED)
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structure_data", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> structureData = Map.of();

    @Column(name = "version", length = 20)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private String templateVersion = "1.0.0";

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public static BlockTemplate create(Warehouse warehouse, AssetCollection collection,
                                     CreateBlockTemplateCommand command,
                                     OffsetDateTime now) {
        BlockTemplate template = BlockTemplate.builder()
                .warehouse(warehouse)
                .collection(collection)
                .name(command.name())
                .description(command.description())
                .thumbnailUrl(command.thumbnailUrl())
                .structureData(command.structureData())
                .templateVersion(command.version())
                .build();
        template.initializeTimestamps(now);
        return template;
    }

    public void update(UpdateBlockTemplateCommand command) {
        if (command.name() != null) this.name = command.name();
        if (command.description() != null) this.description = command.description();
        if (command.thumbnailUrl() != null) this.thumbnailUrl = command.thumbnailUrl();
        if (command.structureData() != null) this.structureData = command.structureData();
        if (command.version() != null) this.templateVersion = command.version();
    }

    public void markAsDeleted(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
