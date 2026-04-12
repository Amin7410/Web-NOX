package com.nox.platform.module.iam.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

/**
 * Domain entity representing a NOX Platform User.
 */
@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.PROTECTED)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private boolean isEmailVerified = false;

    @Column(name = "full_name")
    @Setter
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    @Setter
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "deleted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime deletedAt;

    // --- Relations ---

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @Setter(AccessLevel.PROTECTED)
    private UserSecurity security;

    // --- Domain Behaviors ---

    public void markAsDeleted(OffsetDateTime currentTime) {
        this.deletedAt = currentTime;
        this.status = UserStatus.DELETED;
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
        if (this.status == UserStatus.PENDING_VERIFICATION) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public void linkSecurity(UserSecurity security) {
        this.security = security;
        if (security != null && security.getUser() != this) {
            // No need for setting from security side here as it's handled by MapsId/OneToOne in most JPA configs,
            // but we keep the domain reference correct.
        }
    }
}
