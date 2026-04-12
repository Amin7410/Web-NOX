package com.nox.platform.module.tenant.service.listener;

import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.event.UserAccountDeletingEvent;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantUserCleanupListener {

    private final OrgMemberRepository orgMemberRepository;
    private final TimeProvider timeProvider;

    @EventListener
    @Transactional
    public void onUserAccountDeleting(UserAccountDeletingEvent event) {
        List<OrgMember> memberships = orgMemberRepository.findByUserId(event.userId());

        for (OrgMember member : memberships) {
            // Check for sole owner constraint
            if (member.getRole() != null && "OWNER".equalsIgnoreCase(member.getRole().getName())) {
                long ownerCount = orgMemberRepository.countByOrganizationIdAndRoleName(
                        member.getOrganization().getId(), "OWNER");
                
                if (ownerCount <= 1) {
                    throw new DomainException("CANNOT_DELETE_USER",
                            "User is the sole owner of organization '" + member.getOrganization().getName() + 
                            "'. Transfer ownership or delete the organization first.", 400);
                }
            }
        }

        // Cleanup memberships
        OffsetDateTime now = timeProvider.now();
        for (OrgMember member : memberships) {
            member.softDelete(now);
        }
        orgMemberRepository.saveAll(memberships);
    }
}
