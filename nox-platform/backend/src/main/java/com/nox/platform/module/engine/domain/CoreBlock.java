package com.nox.platform.module.engine.domain;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.shared.exception.DomainException;
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

    public boolean isLockedByOther(UUID userId) {
        if (this.lockedBy == null || this.lockedBy.equals(userId)) {
            return false;
        }
        return this.lockedAt != null && this.lockedAt.plusMinutes(2).isAfter(OffsetDateTime.now());
    }

    public void lock(UUID userId) {
        if (isLockedByOther(userId)) {
            throw new DomainException("BLOCK_LOCKED", "Block is currently locked by another user", 423);
        }
        this.lockedBy = userId;
        this.lockedAt = OffsetDateTime.now();
    }

    public void unlock(UUID userId) {
        if (userId != null && userId.equals(this.lockedBy)) {
            this.lockedBy = null;
            this.lockedAt = null;
        }
    }

    public void updateContent(String name, Map<String, Object> config, Map<String, Object> visual, UUID userId) {
        if (isLockedByOther(userId)) {
            throw new DomainException("BLOCK_LOCKED", "Cannot update: Block is locked by another user", 423);
        }
        if (name != null) this.name = name;
        if (config != null) this.config = config;
        if (visual != null) this.visual = visual;
    }

    public void moveTo(CoreBlock newParent) {
        if (newParent == null) {
            this.parentBlock = null;
            return;
        }

        CoreBlock current = newParent;
        int depth = 1;
        while (current != null) {
            if (current.getId() != null && current.getId().equals(this.getId())) {
                throw new DomainException("CIRCULAR_DEPENDENCY", 
                    "Circular dependency detected: a block cannot be moved inside its own descendant.", 400);
            }
            if (depth >= 10) {
                throw new DomainException("MAX_DEPTH_REACHED", "Cannot nest blocks deeper than 10 levels.", 400);
            }
            current = current.getParentBlock();
            depth++;
        }
        this.parentBlock = newParent;
    }
}
