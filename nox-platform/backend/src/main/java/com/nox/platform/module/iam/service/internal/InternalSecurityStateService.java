package com.nox.platform.module.iam.service.internal;
/* 
 * NOTE: This is a package-private internal service designed to handle security state updates
 * that MUST persist regardless of the status of the main authentication transaction.
 */

import com.nox.platform.module.iam.infrastructure.UserSecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalSecurityStateService {

    private final UserSecurityRepository userSecurityRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedLogins(UUID userId) {
        userSecurityRepository.incrementFailedLogins(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedLogins(UUID userId) {
        userSecurityRepository.resetFailedLogins(userId);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAccount(UUID userId, OffsetDateTime lockedUntil) {
        userSecurityRepository.lockAccount(userId, lockedUntil);
    }
}
