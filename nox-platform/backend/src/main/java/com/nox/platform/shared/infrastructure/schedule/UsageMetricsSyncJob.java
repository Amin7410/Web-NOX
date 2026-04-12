package com.nox.platform.shared.infrastructure.schedule;

import com.nox.platform.module.tenant.infrastructure.OrgUsageMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageMetricsSyncJob {

    private final StringRedisTemplate redisTemplate;
    private final OrgUsageMetricsRepository orgUsageMetricsRepository;
    private final com.nox.platform.shared.abstraction.TimeProvider timeProvider;

    /**
     * Runs every 1 minute.
     * Takes accumulated counters from Redis, flush down to the Postgres, and deletes the Redis key.
     */
    @Scheduled(fixedDelayString = "60000") // 60 seconds
    @Transactional
    public void syncUsageMetricsToDatabase() {
        Set<String> keys = redisTemplate.keys("usage:org:*:*");
        if (keys == null || keys.isEmpty()) {
            return; // No metrics to sync
        }

        log.debug("Found {} metric keys in Redis to flush to DB.", keys.size());

        for (String key : keys) {
            String[] parts = key.split(":");
            if (parts.length != 4) {
                continue;
            }
            
            UUID orgId;
            try {
                orgId = UUID.fromString(parts[2]);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid Org UUID in metric key: {}", key);
                continue;
            }
            String metricType = parts[3];

            // Atomic Get And Delete
            String valStr = redisTemplate.opsForValue().getAndDelete(key);
            if (valStr != null) {
                try {
                    long delta = Long.parseLong(valStr);
                    if (delta > 0) {
                        orgUsageMetricsRepository.incrementMetricUpsert(orgId, metricType, delta, timeProvider.now());
                        log.debug("Flushed metric {} for org {} with delta {}", metricType, orgId, delta);
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid metric value in Redis for key {}: {}", key, valStr);
                }
            }
        }
    }
}
