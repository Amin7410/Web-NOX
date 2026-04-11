package com.nox.platform.module.engine.domain;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
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
import java.util.UUID;

@Entity
@Table(name = "core_blocks")
@SQLDelete(sql = "UPDATE core_blocks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoreBlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private CoreBlock parentBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_asset_id")
    private BlockTemplate originAsset;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 255)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> config = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> visual = Map.of();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "locked_by")
    private UUID lockedBy;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;
}
