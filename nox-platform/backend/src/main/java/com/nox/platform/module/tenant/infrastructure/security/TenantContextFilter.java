package com.nox.platform.module.tenant.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.CustomUserDetails;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Org-Id";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/social-login",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/mfa/verify",
            "/api/v1/auth/mfa/verify-backup",
            "/api/v1/auth/refresh",
            "/api/v1/workspaces/blocks/public"
    );

    private final OrgMemberRepository orgMemberRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

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

        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        if (member.getRole() != null && member.getRole().getPermissions() != null) {
            List<String> permissions = member.getRole().getPermissions();
            permissions.forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission)));
        }

        CustomUserDetails tenantAwareUserDetails = new CustomUserDetails(
                userId,
                orgId,
                userDetails.getUsername(),
                userDetails.getPassword(),
                authorities);

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                tenantAwareUserDetails,
                authentication.getCredentials(),
                authorities);
        newAuth.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        filterChain.doFilter(request, response);
    }
}
