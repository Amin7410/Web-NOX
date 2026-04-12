package com.nox.platform.module.tenant.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "organizations")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Organization extends BaseEntity {

    @Column(nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Column(nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String slug;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> settings = Map.of();

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public static Organization create(String name, String slug, OffsetDateTime now) {
        Organization org = Organization.builder()
                .name(name)
                .slug(slug)
                .settings(Map.of("theme", "system"))
                .build();
        org.initializeTimestamps(now);
        return org;
    }

    public void updateMetadata(String name, String slug) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (slug != null && !slug.isBlank()) {
            this.slug = slug;
        }
    }

    public void updateSettings(Map<String, Object> settings) {
        if (settings != null) {
            this.settings = settings;
        }
    }

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }
}
