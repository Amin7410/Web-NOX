package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrgMemberRepository orgMemberRepository;

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

        // Explicitly delete memberships from the org_members table to avoid HTTP 500s
        // in organization member lists due to Soft-Delete User restriction.
        orgMemberRepository.deleteAll(memberships);

        // Soft delete user (mark as deleted and status = DELETED)
        user.markAsDeleted();
        userRepository.save(user);
    }
}
