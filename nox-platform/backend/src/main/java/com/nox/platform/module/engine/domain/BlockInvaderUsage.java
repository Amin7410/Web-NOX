package com.nox.platform.module.engine.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "block_invader_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockInvaderUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    @JsonIgnore
    private CoreBlock block;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invader_asset_id", nullable = false)
    private InvaderDefinition invaderAsset;

    @Column(name = "applied_version", length = 20)
    private String appliedVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> configSnapshot;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
