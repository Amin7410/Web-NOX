package com.nox.platform.module.engine.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "core_snapshots")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@AttributeOverride(name = "version", column = @Column(name = "version_lock"))
public class CoreSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Project project;

    @Column(nullable = false, length = 255)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Setter(AccessLevel.PROTECTED)
    private String commitMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "full_state_dump", columnDefinition = "jsonb", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private JsonNode fullStateDump;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User createdBy;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public static CoreSnapshot create(Project project, String name, String commitMessage,
                                      JsonNode fullStateDump, User createdBy, OffsetDateTime now) {
        CoreSnapshot snapshot = CoreSnapshot.builder()
                .project(project)
                .name(name)
                .commitMessage(commitMessage)
                .fullStateDump(fullStateDump)
                .createdBy(createdBy)
                .build();
        snapshot.initializeTimestamps(now);
        return snapshot;
    }

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
