package com.nox.platform.module.tenant.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantSecurity")
@RequiredArgsConstructor
@Slf4j
public class TenantSecurity {

    private final OrgMemberRepository orgMemberRepository;
    private final UserRepository userRepository;

    /**
     * Context-aware authorization logic that verifies the current executing user
     * explicitly holds the given permission strictly within the requested Target
     * Organization ID.
     */
    public boolean hasPermission(UUID targetOrgId, String permission) {
        if (targetOrgId == null)
            return false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Check for Global Admin Override (e.g. system admins mapping to '*')
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("*"))) {
            return true;
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null)
            return false;

        OrgMember member = orgMemberRepository.findByOrganizationIdAndUserId(targetOrgId, user.getId()).orElse(null);
        if (member == null || member.getRole() == null || member.getRole().getPermissions() == null) {
            log.warn("IDOR attempt thwarted: User {} attempted accessing Org {} without sufficient membership.",
                    user.getId(), targetOrgId);
            return false;
        }

        boolean hasPerm = member.getRole().getPermissions().contains(permission) ||
                member.getRole().getPermissions().contains("*");

        if (!hasPerm) {
            log.warn("Access Denied: User {} lacks '{}' permission in Org {}", user.getId(), permission, targetOrgId);
        }

        return hasPerm;
    }
}
