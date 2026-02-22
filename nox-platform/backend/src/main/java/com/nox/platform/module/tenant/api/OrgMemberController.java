package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.AddMemberRequest;
import com.nox.platform.module.tenant.api.response.OrgMemberResponse;
import com.nox.platform.module.tenant.api.response.RoleResponse;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.iam.service.InvitationService;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.service.OrgMemberService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/members")
@RequiredArgsConstructor
public class OrgMemberController {

    private final OrgMemberService orgMemberService;
    private final InvitationService invitationService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public ApiResponse<String> inviteMember(
            @PathVariable UUID orgId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.nox.platform.module.tenant.domain.Role role = roleRepository
                .findByOrganizationIdAndName(orgId, request.roleName())
                .orElseThrow(() -> new com.nox.platform.shared.exception.DomainException("ROLE_NOT_FOUND",
                        "Role not found", 404));

        com.nox.platform.module.iam.domain.User inviter = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.nox.platform.shared.exception.DomainException("USER_NOT_FOUND",
                        "Inviter not found", 404));

        invitationService.inviteUser(request.email(), orgId, role.getId(), inviter.getId());

        return ApiResponse.ok("Invitation sent successfully to " + request.email());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:read')")
    public ApiResponse<Page<OrgMemberResponse>> getMembers(
            @PathVariable UUID orgId,
            Pageable pageable) {

        Page<OrgMember> memberPage = orgMemberService.getMembersByOrganization(orgId, pageable);

        // Map Page<OrgMember> to Page<OrgMemberResponse>
        Page<OrgMemberResponse> responsePage = memberPage.map(this::mapToResponse);

        return ApiResponse.ok(responsePage);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public void removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID userId) {

        orgMemberService.removeMember(orgId, userId);
    }

    private OrgMemberResponse mapToResponse(OrgMember member) {
        RoleResponse roleResponse = new RoleResponse(
                member.getRole().getId(),
                member.getRole().getName(),
                member.getRole().getPermissions());

        return new OrgMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getFullName(),
                roleResponse,
                member.getJoinedAt());
    }
}
