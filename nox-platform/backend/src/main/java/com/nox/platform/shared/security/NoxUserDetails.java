package com.nox.platform.shared.security;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;

public interface NoxUserDetails extends UserDetails {
    UUID getId();
}
