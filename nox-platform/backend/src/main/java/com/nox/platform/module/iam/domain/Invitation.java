package com.nox.platform.module.iam.domain;

import com.nox.platform.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "invited_by_id", nullable = false)
    private UUID invitedById;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "resent_count")
    @Builder.Default
    private Integer resentCount = 0;

    @Column(name = "last_sent_at")
    private OffsetDateTime lastSentAt;
}
