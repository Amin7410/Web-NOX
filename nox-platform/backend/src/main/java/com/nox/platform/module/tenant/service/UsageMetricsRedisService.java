package com.nox.platform.module.tenant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsageMetricsRedisService {

    private final StringRedisTemplate redisTemplate;

    public void incrementMetric(UUID orgId, String metricType, long delta) {
        String key = "usage:org:" + orgId.toString() + ":" + metricType;
        redisTemplate.opsForValue().increment(key, delta);
    }
}
