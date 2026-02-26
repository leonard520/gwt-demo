package com.example.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler that maps custom exceptions to structured JSON error responses.
 * <p>
 * Replaces the GWT-RPC exception serialization mechanism.
 * Follows the ErrorResponse schema defined in the API contract.
 * <p>
 * Mappings:
 * <ul>
 *   <li>LoginFailureException → 401 Unauthorized</li>
 *   <li>SessionExpiredException → 401 Unauthorized</li>
 *   <li>ItemServiceException → 500 Internal Server Error</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles LoginFailureException → 401 Unauthorized.
     */
    @ExceptionHandler(LoginFailureException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailure(
            LoginFailureException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles SessionExpiredException → 401 Unauthorized.
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpired(
            SessionExpiredException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles ItemServiceException → 500 Internal Server Error.
     */
    @ExceptionHandler(ItemServiceException.class)
    public ResponseEntity<ErrorResponse> handleItemServiceException(
            ItemServiceException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
