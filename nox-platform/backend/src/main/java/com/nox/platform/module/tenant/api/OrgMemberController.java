package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.AddMemberRequest;
import com.nox.platform.module.tenant.service.command.AddMemberCommand;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.module.tenant.api.response.OrgMemberResponse;
import com.nox.platform.module.tenant.api.response.RoleResponse;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.service.OrgMemberService;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.exception.DomainException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/members")
@RequiredArgsConstructor
public class OrgMemberController {

    private final OrgMemberService orgMemberService;
    private final SecurityProvider securityProvider;

    @PostMapping
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public ResponseEntity<ApiResponse<String>> inviteMember(
            @PathVariable UUID orgId,
            @Valid @RequestBody AddMemberRequest request) {

        String inviterEmail = securityProvider.getCurrentUserEmail()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required", 401));

        AddMemberCommand command = new AddMemberCommand(
                orgId,
                request.email(),
                request.roleName(),
                inviterEmail
        );

        orgMemberService.inviteMember(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Invitation sent successfully to " + request.email()));
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
