package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.nox.platform.shared.abstraction.TimeProvider timeProvider;

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        // Prevent deleting user if they are the sole OWNER of any organization
        List<OrgMember> memberships = orgMemberRepository.findByUserId(userId);

        for (OrgMember member : memberships) {
            if ("OWNER".equals(member.getRole().getName())) {
                long ownerCount = orgMemberRepository.countByOrganizationIdAndRoleName(
                        member.getOrganization().getId(), "OWNER");
                if (ownerCount <= 1) {
                    throw new DomainException("CANNOT_DELETE_USER",
                            "User is the sole owner of organization '" + member.getOrganization().getName() +
                                    "'. Transfer ownership or delete the organization first.",
                            400);
                }
            }
        }

        // Explicitly soft-delete memberships to maintain deterministic time
        OffsetDateTime now = timeProvider.now();
        for (OrgMember member : memberships) {
            member.softDelete(now);
        }
        orgMemberRepository.saveAll(memberships);

        // Soft delete user (mark as deleted and status = DELETED)
        user.markAsDeleted(now);
        userRepository.save(user);

        // Publish event to cleanup related entities (e.g. warehouses)
        eventPublisher.publishEvent(new com.nox.platform.shared.event.UserDeletedEvent(userId));
    }
}
