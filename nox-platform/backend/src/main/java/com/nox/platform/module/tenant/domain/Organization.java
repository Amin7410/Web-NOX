package com.nox.platform.module.tenant.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "organizations")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> settings = Map.of();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Builder
    public Organization(String name, String slug, Map<String, Object> settings) {
        this.name = name;
        this.slug = slug;
        this.settings = settings != null ? settings : Map.of();
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }
}
