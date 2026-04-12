package com.nox.platform.shared.event;

import java.util.UUID;

/**
 * Event published when a new user successfully registers in the platform.
 */
public record UserCreatedEvent(UUID userId, String email) {}
