package com.nox.platform.module.engine.domain;

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
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoreBlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private CoreBlock parentBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_asset_id")
    @Setter(AccessLevel.PROTECTED)
    private com.nox.platform.module.warehouse.domain.BlockTemplate originAsset;

    @Column(nullable = false, length = 100)
    @Setter(AccessLevel.PROTECTED)
    private String type;

    @Column(nullable = false, length = 255)
    @Setter
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> config = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> visual = Map.of();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private com.nox.platform.module.iam.domain.User createdBy;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    @Column(name = "locked_by")
    @Setter(AccessLevel.PROTECTED)
    private UUID lockedBy;

    @Column(name = "locked_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime lockedAt;

    public void lock(UUID userId) {
        if (isLockedByOther(userId)) {
            throw new DomainException("BLOCK_LOCKED", "Block is currently locked by another user", 423);
        }
        this.lockedBy = userId;
        this.lockedAt = OffsetDateTime.now();
    }

    public void unlock(UUID userId) {
        if (isLockedByOther(userId)) {
            throw new DomainException("BLOCK_LOCKED", "You cannot unlock a block locked by someone else", 403);
        }
        this.lockedBy = null;
        this.lockedAt = null;
    }

    public boolean isLockedByOther(UUID userId) {
        if (this.lockedBy == null) return false;
        if (this.lockedBy.equals(userId)) return false;
        
        return this.lockedAt.isAfter(OffsetDateTime.now().minusMinutes(2));
    }

    public void updateContent(String name, Map<String, Object> config, Map<String, Object> visual, UUID userId) {
        if (isLockedByOther(userId)) {
            throw new DomainException("BLOCK_LOCKED", "Cannot update content while block is locked by another user", 423);
        }
        if (name != null) this.name = name;
        if (config != null) this.config = config;
        if (visual != null) this.visual = visual;
    }

    public void moveTo(CoreBlock newParent) {
        validateParentAssignment(newParent, this);
        this.parentBlock = newParent;
    }

    private void validateParentAssignment(CoreBlock parent, CoreBlock current) {
        if (parent == null) return;
        
        if (parent.getId() != null && parent.getId().equals(current.getId())) {
            throw new DomainException("CIRCULAR_DEPENDENCY", "A block cannot be its own parent", 400);
        }

        CoreBlock temp = parent;
        int depth = 0;
        while (temp.getParentBlock() != null) {
            if (current.getId() != null && temp.getParentBlock().getId().equals(current.getId())) {
                throw new DomainException("CIRCULAR_DEPENDENCY", "Circular block hierarchy detected", 400);
            }
            temp = temp.getParentBlock();
            depth++;
            if (depth > 10) {
                throw new DomainException("MAX_DEPTH_REACHED", "Maximum block depth (10) exceeded", 400);
            }
        }
    }
}
