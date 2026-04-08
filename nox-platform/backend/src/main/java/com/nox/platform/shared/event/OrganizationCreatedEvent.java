package com.nox.platform.shared.event;

import java.util.UUID;

/**
 * Event published when a new organization is successfully created.
 */
public record OrganizationCreatedEvent(UUID organizationId, UUID creatorId) {}
