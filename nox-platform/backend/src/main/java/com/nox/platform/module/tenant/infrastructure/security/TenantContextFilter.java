package com.nox.platform.module.tenant.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Org-Id";
    private final OrgMemberRepository orgMemberRepository;
    private final UserRepository userRepository;

    // Danh sách các Endpoint công khai không áp dụng Tenant Filter (để tránh chặn login/register)
    private static final java.util.List<String> PUBLIC_PATHS = java.util.List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/social-login",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/mfa/verify",
            "/api/v1/auth/mfa/verify-backup",
            "/api/v1/auth/refresh",
            "/api/v1/workspaces/blocks/public" // Nếu có
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        
        // 1. Bypass Filter nếu là Endpoint công khai
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenantIdHeader = request.getHeader(TENANT_HEADER);
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID orgId;
        try {
            orgId = UUID.fromString(tenantIdHeader);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Org-Id format");
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("Tenant: Unauthorized access attempt. Auth: {}, Authenticated: {}", 
                authentication == null ? "NULL" : "PRESENT",
                authentication == null ? "N/A" : authentication.isAuthenticated());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TENANT: Authentication required for tenant access");
            return;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        UUID originalUserId = null;
        if (userDetails instanceof com.nox.platform.shared.security.NoxUserDetails noxUser) {
            originalUserId = noxUser.getId();
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Tenant: User context not found for email: {}", email);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "TENANT: User context not found for " + email);
            return;
        }

        UUID userId = user.getId();

        OrgMember member = orgMemberRepository.findByOrganizationIdAndUserId(orgId, userId).orElse(null);
        if (member == null) {
            log.warn("User {} attempted cross-tenant access to Organization {}", userId, orgId);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this organization");
            return;
        }

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>(
                authentication.getAuthorities());
        if (member.getRole() != null && member.getRole().getPermissions() != null) {
            java.util.List<String> permissions = member.getRole().getPermissions();
            
            // Handle Wildcard Permissions (* or ALL)
            if (permissions.contains("*") || permissions.contains("ALL")) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("workspace:manage"));
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("workspace:read"));
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("iam:manage"));
            }
            
            permissions.forEach(permission -> authorities
                    .add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission)));
        }

        com.nox.platform.module.iam.infrastructure.security.CustomUserDetails tenantAwareUserDetails = new com.nox.platform.module.iam.infrastructure.security.CustomUserDetails(
                userId,
                orgId,
                userDetails.getUsername(),
                userDetails.getPassword(),
                authorities);

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                tenantAwareUserDetails,
                authentication.getCredentials(),
                authorities);
        newAuth.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        filterChain.doFilter(request, response);
    }
}
