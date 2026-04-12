package com.nox.platform.module.warehouse.domain;

import com.nox.platform.module.warehouse.service.command.CreateInvaderDefinitionCommand;
import com.nox.platform.module.warehouse.service.command.UpdateInvaderDefinitionCommand;
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
@Table(name = "assets_invader_definitions")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@AttributeOverride(name = "version", column = @Column(name = "version_lock"))
public class InvaderDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    @Setter(AccessLevel.PROTECTED)
    private AssetCollection collection;

    @Column(name = "code", nullable = false, length = 100)
    @Setter(AccessLevel.PROTECTED)
    private String code;

    @Column(name = "name", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(name = "category", nullable = false, length = 50)
    @Setter(AccessLevel.PROTECTED)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_schema", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> configSchema = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compiler_hooks", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> compilerHooks = Map.of();

    @Column(name = "version", length = 20)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private String templateVersion = "1.0.0";

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public static InvaderDefinition create(Warehouse warehouse, AssetCollection collection,
                                         CreateInvaderDefinitionCommand command,
                                         OffsetDateTime now) {
        InvaderDefinition definition = InvaderDefinition.builder()
                .warehouse(warehouse)
                .collection(collection)
                .code(command.code())
                .name(command.name())
                .category(command.category())
                .configSchema(command.configSchema())
                .compilerHooks(command.compilerHooks())
                .templateVersion(command.version())
                .build();
        definition.initializeTimestamps(now);
        return definition;
    }

    public void update(UpdateInvaderDefinitionCommand command) {
        if (command.name() != null) this.name = command.name();
        if (command.category() != null) this.category = command.category();
        if (command.configSchema() != null) this.configSchema = command.configSchema();
        if (command.compilerHooks() != null) this.compilerHooks = command.compilerHooks();
        if (command.version() != null) this.templateVersion = command.version();
    }

    public void markAsDeleted(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
