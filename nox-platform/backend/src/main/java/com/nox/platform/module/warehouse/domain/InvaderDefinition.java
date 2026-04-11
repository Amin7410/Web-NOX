package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "assets_invader_definitions")
@SQLDelete(sql = "UPDATE assets_invader_definitions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@AttributeOverride(name = "version", column = @Column(name = "version_lock"))
public class InvaderDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private AssetCollection collection;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_schema", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> configSchema = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compiler_hooks", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> compilerHooks = Map.of();

    @Column(name = "version", length = 20)
    @Builder.Default
    private String templateVersion = "1.0.0";

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
