package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WarehouseAccessValidatorImpl implements WarehouseAccessValidator {

    private final SecurityProvider securityProvider;
    private final OrgMemberRepository orgMemberRepository;

    @Override
    public void validateReadAccess(UUID targetOwnerId, OwnerType ownerType) {
        validateAccess(targetOwnerId, ownerType, false);
    }

    @Override
    public void validateWriteAccess(UUID targetOwnerId, OwnerType ownerType) {
        validateAccess(targetOwnerId, ownerType, true);
    }

    private void validateAccess(UUID targetOwnerId, OwnerType ownerType, boolean writeRequired) {
        UUID currentUserId = securityProvider.getCurrentUserId()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required", 401));

        if (ownerType == OwnerType.USER) {
            if (!currentUserId.equals(targetOwnerId)) {
                throw new DomainException("FORBIDDEN", "User warehouse access denied", 403);
            }
        } else if (ownerType == OwnerType.ORG) {
            var member = orgMemberRepository.findByOrganizationIdAndUserId(targetOwnerId, currentUserId)
                    .orElseThrow(() -> new DomainException("FORBIDDEN", "Organization membership required", 403));

            if (writeRequired && !member.hasPermission("workspace:manage")) {
                throw new DomainException("FORBIDDEN", "Insufficient organization permissions", 403);
            }
        }
    }
}
