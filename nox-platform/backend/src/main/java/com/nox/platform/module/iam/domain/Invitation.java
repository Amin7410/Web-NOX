package com.nox.platform.module.iam.domain;

import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Invitation extends BaseEntity {

    @Column(nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String email;

    @Column(name = "org_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private UUID orgId;

    @Column(name = "role_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private UUID roleId;

    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.PROTECTED)
    private String token;

    @Column(name = "invited_by_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private UUID invitedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime acceptedAt;

    @Column(name = "resent_count")
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private Integer resentCount = 0;

    @Column(name = "last_sent_at")
    @Setter(AccessLevel.PROTECTED)
    private OffsetDateTime lastSentAt;

    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(OffsetDateTime.now());
    }

    public void accept(User user) {
        if (this.status != InvitationStatus.PENDING) {
            throw new DomainException("INVITATION_NOT_PENDING", "This invitation is no longer pending", 400);
        }

        if (isExpired()) {
            this.status = InvitationStatus.EXPIRED;
            throw new DomainException("INVITATION_EXPIRED", "This invitation has expired", 400);
        }

        if (user == null || !user.getEmail().equalsIgnoreCase(this.email)) {
            throw new DomainException("EMAIL_MISMATCH", "This invitation was sent to a different email address", 403);
        }

        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = OffsetDateTime.now();
    }
}
