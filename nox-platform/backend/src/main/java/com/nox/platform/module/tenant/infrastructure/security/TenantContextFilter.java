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

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required for tenant access");
            return;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User context not found");
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
            member.getRole().getPermissions().forEach(permission -> authorities
                    .add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission)));
        }

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                authorities);
        newAuth.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        filterChain.doFilter(request, response);
    }
}
