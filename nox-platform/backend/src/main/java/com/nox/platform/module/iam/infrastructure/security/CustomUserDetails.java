package com.nox.platform.module.iam.infrastructure.security;

import com.nox.platform.shared.security.NoxUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails extends User implements NoxUserDetails {

    private final UUID id;
    private final UUID orgId;

    public CustomUserDetails(UUID id, UUID orgId, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.orgId = orgId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getOrganizationId() {
        return orgId;
    }
}
