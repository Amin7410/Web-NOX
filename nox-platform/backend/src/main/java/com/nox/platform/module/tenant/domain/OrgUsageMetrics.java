package com.nox.platform.module.tenant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "org_usage_metrics")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgUsageMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    @Column(name = "current_value")
    private Long currentValue = 0L;

    @Column(name = "reset_at")
    private OffsetDateTime resetAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public OrgUsageMetrics(UUID orgId, String metricType) {
        this.orgId = orgId;
        this.metricType = metricType;
        this.currentValue = 0L;
    }
}
