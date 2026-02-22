package com.nox.platform.module.tenant.domain;

import com.nox.platform.module.iam.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "org_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_org_id_user_id", columnNames = { "org_id", "user_id" })
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Builder
    public OrgMember(Organization organization, User user, Role role, User invitedBy) {
        this.organization = organization;
        this.user = user;
        this.role = role;
        this.invitedBy = invitedBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.joinedAt == null) {
            this.joinedAt = OffsetDateTime.now();
        }
    }
}
