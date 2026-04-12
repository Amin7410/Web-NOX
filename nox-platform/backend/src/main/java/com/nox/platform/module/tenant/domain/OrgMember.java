package com.nox.platform.module.tenant.domain;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "org_members")
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
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime joinedAt;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    public void softDelete(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
    }

    public static OrgMember create(Organization organization, User user, Role role, User invitedBy, OffsetDateTime now) {
        OrgMember member = OrgMember.builder()
                .organization(organization)
                .user(user)
                .role(role)
                .invitedBy(invitedBy)
                .joinedAt(now)
                .build();
        member.initializeTimestamps(now);
        return member;
    }

    public boolean canAssignRole(Role targetRole) {
        if (this.role == null || targetRole == null) return false;
        return this.role.getLevel() >= targetRole.getLevel();
    }

    public void changeRole(Role newRole, OrgMember manager) {
        if (manager == null || !manager.canAssignRole(newRole)) {
            throw new DomainException("INSUFFICIENT_PRIVILEGE", "Higher level role assignment forbidden");
        }
        this.role = newRole;
    }

    public boolean hasPermission(String permission) {
        if (this.role == null) return false;
        return this.role.getPermissions().contains("*") || 
               this.role.getPermissions().contains(permission);
    }
}

