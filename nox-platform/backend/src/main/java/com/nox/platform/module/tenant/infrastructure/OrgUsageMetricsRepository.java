package com.nox.platform.module.tenant.infrastructure;

import com.nox.platform.module.tenant.domain.OrgUsageMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrgUsageMetricsRepository extends JpaRepository<OrgUsageMetrics, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO org_usage_metrics (id, org_id, metric_type, current_value, updated_at)
            VALUES (gen_random_uuid(), :orgId, :metricType, :delta, NOW())
            ON CONFLICT (org_id, metric_type) 
            DO UPDATE SET current_value = org_usage_metrics.current_value + :delta, updated_at = NOW()
            """, nativeQuery = true)
    void incrementMetricUpsert(@Param("orgId") UUID orgId, @Param("metricType") String metricType, @Param("delta") long delta);
}
