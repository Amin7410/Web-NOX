package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.CreateOrganizationRequest;
import com.nox.platform.module.tenant.api.request.UpdateOrganizationRequest;
import com.nox.platform.module.tenant.api.response.OrganizationResponse;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Organization org = organizationService.createOrganization(request.name(), userDetails.getUsername());
        return ApiResponse.ok(mapToResponse(org));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#id, 'workspace:read')")
    public ApiResponse<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        Organization org = organizationService.getOrganizationById(id);
        return ApiResponse.ok(mapToResponse(org));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#id, 'workspace:manage')")
    public ApiResponse<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {

        Organization org = organizationService.updateOrganization(id, request.name(), request.settings());
        return ApiResponse.ok(mapToResponse(org));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('*') or @tenantSecurity.hasPermission(#id, 'workspace:manage')")
    public void deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getSettings(),
                org.getCreatedAt(),
                org.getUpdatedAt());
    }
}
