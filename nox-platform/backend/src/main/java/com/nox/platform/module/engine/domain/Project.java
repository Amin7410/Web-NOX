package com.nox.platform.module.engine.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "org_id", "slug" }, name = "idx_projects_org_slug")
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    @JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Organization organization;

    @Column(nullable = false, length = 255)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(nullable = false, length = 255)
    @Setter(AccessLevel.PROTECTED)
    private String slug;

    @Column(columnDefinition = "TEXT")
    @Setter
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    @Setter
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User createdBy;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "project", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @Builder.Default
    @JsonIgnore
    private List<Workspace> workspaces = new ArrayList<>();

    // --- Domain Methods (Stage 2) ---

    public void updateMetadata(String name, String slug, String description, 
                              ProjectVisibility visibility, ProjectStatus status) {
        if (name != null) this.name = name;
        if (slug != null) this.slug = slug;
        if (description != null) this.description = description;
        if (visibility != null) this.visibility = visibility;
        if (status != null) this.status = status;
    }

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
