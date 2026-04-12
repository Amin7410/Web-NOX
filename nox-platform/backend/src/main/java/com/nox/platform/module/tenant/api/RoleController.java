package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.CreateRoleRequest;
import com.nox.platform.module.tenant.api.request.UpdateRoleRequest;
import com.nox.platform.module.tenant.api.response.RoleResponse;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.service.RoleService;
import com.nox.platform.module.tenant.service.command.CreateRoleCommand;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateRoleRequest request) {

        CreateRoleCommand command = new CreateRoleCommand(
                orgId,
                request.name(),
                request.permissions(),
                request.level()
        );

        Role role = roleService.createRole(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapToResponse(role)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:read')")
    public ApiResponse<List<RoleResponse>> getRoles(@PathVariable UUID orgId) {
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

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#orgId, 'iam:manage')")
    public ApiResponse<RoleResponse> updateRolePermissions(
            @PathVariable UUID orgId,
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        Role updatedRole = roleService.updatePermissions(orgId, roleId, request.permissions());
        return ApiResponse.ok(mapToResponse(updatedRole));
    }

    private RoleResponse mapToResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getPermissions());
    }
}
