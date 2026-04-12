package com.nox.platform.shared.infrastructure.security;

import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.security.NoxUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SpringSecurityProvider implements SecurityProvider {

    @Override
    public Optional<UUID> getCurrentUserId() {
        return getPrincipal()
                .map(NoxUserDetails::getId);
    }

    @Override
    public Optional<UUID> getCurrentOrganizationId() {
        return getPrincipal()
                .map(NoxUserDetails::getOrganizationId);
    }

    @Override
    public Optional<String> getCurrentUserEmail() {
        return getPrincipal()
                .map(NoxUserDetails::getUsername);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    @Override
    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private Optional<NoxUserDetails> getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NoxUserDetails details) {
            return Optional.of(details);
        }
        return Optional.empty();
    }
}
