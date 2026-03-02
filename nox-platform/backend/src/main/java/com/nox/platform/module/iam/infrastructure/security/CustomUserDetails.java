package com.nox.platform.module.iam.infrastructure.security;

import com.nox.platform.shared.security.NoxUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails extends User implements NoxUserDetails {

    private final UUID id;

    public CustomUserDetails(UUID id, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
