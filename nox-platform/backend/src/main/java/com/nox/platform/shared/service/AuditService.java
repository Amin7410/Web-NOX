package com.nox.platform.shared.service;

import com.nox.platform.shared.domain.AuditLog;
import com.nox.platform.shared.infrastructure.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID orgId, UUID actorId, String action, String targetType, UUID targetId,
            Map<String, Object> metadata, String ip, String ua) {
        AuditLog log = AuditLog.builder()
                .orgId(orgId)
                .actorId(actorId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .metadata(metadata)
                .ipAddress(ip)
                .userAgent(ua)
                .build();
        auditLogRepository.save(log);
    }
}
