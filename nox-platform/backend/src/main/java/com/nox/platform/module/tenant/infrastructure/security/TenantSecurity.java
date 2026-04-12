package com.nox.platform.module.tenant.infrastructure.security;

import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantSecurity")
@RequiredArgsConstructor
@Slf4j
public class TenantSecurity {

    private final OrgMemberRepository orgMemberRepository;
    private final com.nox.platform.shared.abstraction.SecurityProvider securityProvider;

    public boolean hasPermission(UUID targetOrgId, String permission) {
        if (targetOrgId == null) return false;

        java.util.UUID currentUserId = securityProvider.getCurrentUserId().orElse(null);
        if (currentUserId == null) return false;

        if (securityProvider.hasAuthority("*")) return true;

        return orgMemberRepository.findByOrganizationIdAndUserId(targetOrgId, currentUserId)
                .map(member -> member.hasPermission(permission))
                .orElse(false);
    }
}
