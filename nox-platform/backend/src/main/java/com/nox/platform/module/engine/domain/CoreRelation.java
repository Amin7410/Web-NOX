package com.nox.platform.module.engine.domain;

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
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoreRelation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_block_id", nullable = false)
    private CoreBlock sourceBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_block_id", nullable = false)
    private CoreBlock targetBlock;

    @Column(nullable = false, length = 50)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> rules = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> visual = Map.of();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
        this.updateTimestamp(currentTime);
    }
}
