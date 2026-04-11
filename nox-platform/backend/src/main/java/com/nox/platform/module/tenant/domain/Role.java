package com.nox.platform.module.tenant.domain;

import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Role extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 100)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    @Builder.Default
    private List<String> permissions = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private int level = 0;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void softDelete() {
        if (isOwnerRole()) {
            throw new DomainException("IMMUTABLE_ROLE", "The OWNER role cannot be deleted", 400);
        }
        this.deletedAt = OffsetDateTime.now();
    }

    public boolean isOwnerRole() {
        return "OWNER".equalsIgnoreCase(this.name);
    }

    public void updatePermissions(List<String> newPermissions) {
        if (isOwnerRole()) {
            throw new DomainException("IMMUTABLE_ROLE", "Cannot modify permissions of the OWNER role", 400);
        }
        this.permissions = newPermissions != null ? newPermissions : new ArrayList<>();
    }

    public boolean canBeManagedBy(Role managerRole) {
        if (managerRole == null) return false;
        return managerRole.getLevel() >= this.level;
    }
}
