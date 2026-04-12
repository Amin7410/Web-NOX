package com.nox.platform.module.tenant.api;

import com.nox.platform.module.tenant.api.request.CreateOrganizationRequest;
import com.nox.platform.module.tenant.api.request.UpdateOrganizationRequest;
import com.nox.platform.module.tenant.service.command.CreateOrganizationCommand;
import com.nox.platform.module.tenant.service.command.UpdateOrganizationCommand;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.module.tenant.api.response.OrganizationResponse;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.exception.DomainException;
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
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final SecurityProvider securityProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        String userEmail = securityProvider.getCurrentUserEmail()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required", 401));

        CreateOrganizationCommand command = new CreateOrganizationCommand(request.name(), userEmail);
        Organization org = organizationService.createOrganization(command);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapToResponse(org)));
    }

    @GetMapping
    public ApiResponse<List<OrganizationResponse>> getOrganizations() {
        String userEmail = securityProvider.getCurrentUserEmail()
                .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required", 401));

        List<Organization> orgs = organizationService.getOrganizationsForUser(userEmail);
        return ApiResponse.ok(orgs.stream().map(this::mapToResponse).collect(Collectors.toList()));
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

        UpdateOrganizationCommand command = new UpdateOrganizationCommand(id, request.name(), request.settings());
        Organization org = organizationService.updateOrganization(command);
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
