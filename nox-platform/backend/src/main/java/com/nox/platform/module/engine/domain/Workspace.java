package com.nox.platform.module.engine.domain;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workspaces")
@SQLDelete(sql = "UPDATE workspaces SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Workspace extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Project project;

    @Column(nullable = false, length = 255)
    @Setter
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Setter(AccessLevel.PROTECTED)
    private WorkspaceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private WorkspaceStatus status = WorkspaceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User createdBy;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    // --- Domain Methods (Stage 2) ---

    public void updateMetadata(String name, WorkspaceType type) {
        if (name != null) this.name = name;
        if (type != null) this.type = type;
    }

    public void updateStatus(WorkspaceStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }
}
