package com.nox.platform.module.engine.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "core_blocks")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoreBlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    @JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private CoreBlock parentBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_asset_id")
    @Setter(AccessLevel.PROTECTED)
    private BlockTemplate originAsset;

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
    @JoinColumn(name = "created_by_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User createdBy;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    @Column(name = "locked_by")
    @Setter(AccessLevel.PROTECTED)
    private UUID lockedBy;

    @Column(name = "locked_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime lockedAt;

    public void lock(UUID userId, OffsetDateTime currentTime) {
        if (isLockedByOther(userId, currentTime)) {
            throw new DomainException("BLOCK_LOCKED", "Block is currently locked by another user");
        }
        this.lockedBy = userId;
        this.lockedAt = currentTime;
        this.updateTimestamp(currentTime);
    }

    public void unlock(UUID userId, OffsetDateTime currentTime) {
        if (isLockedByOther(userId, currentTime)) {
            throw new DomainException("BLOCK_LOCKED", "You cannot unlock a block locked by someone else");
        }
        this.lockedBy = null;
        this.lockedAt = null;
        this.updateTimestamp(currentTime);
    }

    public boolean isLockedByOther(UUID userId, OffsetDateTime currentTime) {
        if (this.lockedBy == null) return false;
        if (this.lockedBy.equals(userId)) return false;
        
        return this.lockedAt.isAfter(currentTime.minusMinutes(2));
    }

    public void updateContent(String name, Map<String, Object> config, Map<String, Object> visual, UUID userId, OffsetDateTime currentTime) {
        if (isLockedByOther(userId, currentTime)) {
            throw new DomainException("BLOCK_LOCKED", "Cannot update content while block is locked by another user");
        }
        if (name != null) this.name = name;
        if (config != null) this.config = config;
        if (visual != null) this.visual = visual;
        this.updateTimestamp(currentTime);
    }

    public void moveTo(CoreBlock newParent) {
        validateParentAssignment(newParent, this);
        this.parentBlock = newParent;
    }

    private void validateParentAssignment(CoreBlock parent, CoreBlock current) {
        if (parent == null) return;
        
        if (parent.getId() != null && parent.getId().equals(current.getId())) {
            throw new DomainException("CIRCULAR_DEPENDENCY", "A block cannot be its own parent");
        }

        CoreBlock temp = parent;
        int depth = 0;
        while (temp.getParentBlock() != null) {
            if (current.getId() != null && temp.getParentBlock().getId().equals(current.getId())) {
                throw new DomainException("CIRCULAR_DEPENDENCY", "Circular block hierarchy detected");
            }
            temp = temp.getParentBlock();
            depth++;
            if (depth > 10) {
                throw new DomainException("MAX_DEPTH_REACHED", "Maximum block depth (10) exceeded");
            }
        }
    }

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
        this.updateTimestamp(currentTime);
    }
}

