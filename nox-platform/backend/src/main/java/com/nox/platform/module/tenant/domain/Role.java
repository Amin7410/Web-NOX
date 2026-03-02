package com.nox.platform.module.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "roles")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 100)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> permissions = new ArrayList<>();

    @Column(nullable = false)
    private int level = 0;

    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;

    @Builder
    public Role(Organization organization, String name, List<String> permissions, Integer level) {
        this.organization = organization;
        this.name = name;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.level = level != null ? level : 0;
    }

    public void softDelete() {
        this.deletedAt = java.time.OffsetDateTime.now();
    }
}
