package com.nox.platform.shared.exception;

import lombok.Getter;

/**
 * Base exception class indicating a business rule violation or known error
 * state.
 * To be thrown from the Service layer (application/domain).
 */
@Getter
public class DomainException extends RuntimeException {

    private final String code;
    private final int status;

    public DomainException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public DomainException(String code, String message) {
        this(code, message, 400); // Default to bad request for domain errors
    }
}
