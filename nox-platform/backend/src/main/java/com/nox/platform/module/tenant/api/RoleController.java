package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.CreateRoleRequest;
import com.nox.platform.module.tenant.api.response.RoleResponse;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.module.tenant.service.RoleService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public ApiResponse<RoleResponse> createRole(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateRoleRequest request) {

        Organization org = organizationService.getOrganizationById(orgId);
        Role role = roleService.createRole(org, request.name(), request.permissions());

        return ApiResponse.ok(mapToResponse(role));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:read')")
    public ApiResponse<List<RoleResponse>> getRoles(@PathVariable UUID orgId) {
        // Validate organization exists before fetching roles
        organizationService.getOrganizationById(orgId);
        List<Role> roles = roleService.getRolesByOrganization(orgId);

        List<RoleResponse> roleResponses = roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.ok(roleResponses);
    }

    @DeleteMapping("/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public void deleteRole(
            @PathVariable UUID orgId,
            @PathVariable String roleName) {
        roleService.deleteRole(orgId, roleName);
    }

    private RoleResponse mapToResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getPermissions());
    }
}
