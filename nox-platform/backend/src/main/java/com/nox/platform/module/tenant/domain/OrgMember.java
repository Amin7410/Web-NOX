package com.nox.platform.module.tenant.domain;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "org_members")
@SQLDelete(sql = "UPDATE org_members SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrgMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    @Setter(AccessLevel.PROTECTED)
    private User invitedBy;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    // --- Domain Methods (Stage 4) ---

    public boolean canAssignRole(Role targetRole) {
        if (this.role == null || targetRole == null) return false;
        return this.role.getLevel() >= targetRole.getLevel();
    }

    public void changeRole(Role newRole, OrgMember manager) {
        if (manager == null || !manager.canAssignRole(newRole)) {
            throw new DomainException("INSUFFICIENT_PRIVILEGE", 
                "You cannot assign a role with a higher level than your own", 403);
        }
        this.role = newRole;
    }
}
