package com.nox.platform.shared.abstraction;

import java.util.Optional;
import java.util.UUID;

public interface SecurityProvider {

    Optional<UUID> getCurrentUserId();

    Optional<UUID> getCurrentOrganizationId();

    Optional<String> getCurrentUserEmail();

    boolean isAuthenticated();

    boolean hasAuthority(String authority);
}
