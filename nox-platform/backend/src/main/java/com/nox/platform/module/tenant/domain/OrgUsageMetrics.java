package com.nox.platform.module.tenant.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "org_usage_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrgUsageMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    @Column(name = "current_value")
    @Builder.Default
    private Long currentValue = 0L;

    @Column(name = "reset_at")
    private OffsetDateTime resetAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static OrgUsageMetrics create(UUID orgId, String metricType, OffsetDateTime now) {
        return OrgUsageMetrics.builder()
                .orgId(orgId)
                .metricType(metricType)
                .currentValue(0L)
                .updatedAt(now)
                .build();
    }

    public void increment(long amount, OffsetDateTime now) {
        this.currentValue += amount;
        this.updatedAt = now;
    }

    public void reset(OffsetDateTime now) {
        this.currentValue = 0L;
        this.resetAt = now;
        this.updatedAt = now;
    }

    public void updateTimestamp(OffsetDateTime now) {
        this.updatedAt = now;
    }
}
