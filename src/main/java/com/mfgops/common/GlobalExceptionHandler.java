package com.mfgops.common;

import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.common.exception.TenantMismatchException;
import com.mfgops.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.failure(List.of(ex.getMessage()), "Resource not found"));
    }

    @ExceptionHandler(TenantMismatchException.class)
    public ResponseEntity<StandardResponse<Void>> handleTenantMismatch(TenantMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.failure(List.of(ex.getMessage()), "Tenant validation failed"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StandardResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.failure(List.of(ex.getMessage()), "Unauthorized access"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.failure(List.of("Invalid username or password"), "Authentication failed"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.failure(List.of("You do not have permission to access this resource"), "Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.failure(errors, "Validation failed"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.failure(List.of(ex.getMessage()), "Bad request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleGeneralException(Exception ex) {
        ex.printStackTrace(); // Log the stacktrace for backend diagnostics
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.failure(List.of("An unexpected error occurred on the server"), "Server error"));
    }
}
