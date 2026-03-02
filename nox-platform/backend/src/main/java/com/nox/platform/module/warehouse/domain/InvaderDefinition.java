package com.nox.platform.module.warehouse.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "assets_invader_definitions")
@SQLDelete(sql = "UPDATE assets_invader_definitions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Map<String, Object> configSchema = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compiler_hooks", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> compilerHooks = Map.of();

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public InvaderDefinition(Warehouse warehouse, AssetCollection collection, String code, String name, String category,
            Map<String, Object> configSchema, Map<String, Object> compilerHooks, String version) {
        this.warehouse = warehouse;
        this.collection = collection;
        this.code = code;
        this.name = name;
        this.category = category;
        this.configSchema = configSchema != null ? configSchema : Map.of();
        this.compilerHooks = compilerHooks != null ? compilerHooks : Map.of();
        this.version = version != null ? version : "1.0.0";
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
