package com.nox.platform.shared.infrastructure.config;

import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<String> FORBIDDEN_CODES = Set.of(
            "FORBIDDEN", 
            "INSUFFICIENT_PRIVILEGE", 
            "ACCOUNT_LOCKED", 
            "ACCOUNT_NOT_ACTIVE"
    );

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        String code = ex.getCode();
        HttpStatus status = mapCodeToStatus(code);
        
        ApiResponse<Void> response = ApiResponse.error(code, ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    private HttpStatus mapCodeToStatus(String code) {
        if (code == null) return HttpStatus.BAD_REQUEST;
        
        if (code.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        
        if (code.equals("UNAUTHORIZED")) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        if (FORBIDDEN_CODES.contains(code)) {
            return HttpStatus.FORBIDDEN;
        }
        
        if (code.equals("BLOCK_LOCKED")) {
            return HttpStatus.LOCKED;
        }
        
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        ApiResponse<Void> response = ApiResponse.error("UNAUTHORIZED", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", "The requested endpoint was not found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ApiResponse<Void> response = ApiResponse.error("VALIDATION_ERROR", "Invalid request parameters",
                errors.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse<Void> response = ApiResponse.error("FORBIDDEN", "You do not have permission to perform this action");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse<Void> response = ApiResponse.error("MALFORMED_JSON", "Malformed JSON request");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        ApiResponse<Void> response = ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred", ex.toString());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
