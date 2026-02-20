package com.nox.platform.shared.exception;

/**
 * Specifically thrown when an entity is not found by ID or other unique
 * identifier.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("NOT_FOUND", String.format("%s with identifier '%s' was not found", resourceType, identifier), 404);
    }
}
