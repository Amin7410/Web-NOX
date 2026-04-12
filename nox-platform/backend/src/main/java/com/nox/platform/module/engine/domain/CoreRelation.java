package com.nox.platform.module.engine.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "core_relations")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoreRelation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_block_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private CoreBlock sourceBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_block_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private CoreBlock targetBlock;

    @Column(nullable = false, length = 50)
    @Setter(AccessLevel.PROTECTED)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> rules = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> visual = Map.of();

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public void update(Map<String, Object> rules, Map<String, Object> visual) {
        if (rules != null) this.rules = rules;
        if (visual != null) this.visual = visual;
    }

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
        this.updateTimestamp(currentTime);
    }
}
