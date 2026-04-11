package com.nox.platform.shared.infrastructure.schedule;

import com.nox.platform.module.iam.infrastructure.OtpCodeRepository;
import com.nox.platform.module.iam.infrastructure.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final UserSessionRepository userSessionRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final com.nox.platform.module.engine.infrastructure.CoreRelationRepository coreRelationRepository;
    private final com.nox.platform.module.engine.infrastructure.CoreBlockRepository coreBlockRepository;
    private final com.nox.platform.module.engine.infrastructure.WorkspaceRepository workspaceRepository;

    /**
     * Purge expired sessions and unused OTPs every day at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void performMaintenance() {
        log.info("Starting scheduled maintenance: Purging expired data...");

        OffsetDateTime threshold = OffsetDateTime.now().minusDays(30);

        // 1. Delete sessions that expired or were revoked more than 30 days ago
        int deletedSessions = userSessionRepository.deleteByExpiresAtBeforeOrRevokedAtBefore(threshold, threshold);
        log.info("Purged {} expired/revoked sessions older than 30 days", deletedSessions);

        // 2. Delete OTP codes that expired or were used more than 30 days ago
        int deletedOtps = otpCodeRepository.deleteByExpiresAtBeforeOrUsedAtBefore(threshold, threshold);
        log.info("Purged {} expired/used OTP codes older than 30 days", deletedOtps);

        // 3. Purge soft-deleted Workspaces (And their blocks/relations) in careful batches to avoid DB Locks
        purgeSoftDeletedWorkspaces(threshold);
    }

    private void purgeSoftDeletedWorkspaces(OffsetDateTime threshold) {
        log.info("Starting batch hard-delete for workspaces soft-deleted before {}", threshold);
        int batchSize = 500;
        int totalRelations = 0, totalBlocks = 0, totalWorkspaces = 0;

        // Bottom-up: Relations -> Blocks -> Workspaces
        int deleted;
        do {
            deleted = coreRelationRepository.deleteOldRelationsInBatch(threshold, batchSize);
            totalRelations += deleted;
        } while (deleted == batchSize);

        do {
            deleted = coreBlockRepository.deleteOldBlocksInBatch(threshold, batchSize);
            totalBlocks += deleted;
        } while (deleted == batchSize);

        do {
            deleted = workspaceRepository.deleteOldWorkspacesInBatch(threshold, batchSize);
            totalWorkspaces += deleted;
        } while (deleted == batchSize);

        log.info("Completed batch hard-delete. Removed {} relations, {} blocks, and {} workspaces.", totalRelations, totalBlocks, totalWorkspaces);
    }
}
