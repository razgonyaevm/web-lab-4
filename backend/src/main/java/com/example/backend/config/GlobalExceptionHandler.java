package com.example.backend.config;

import com.example.backend.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ApiResponse> handleValidationExceptions(WebExchangeBindException ex) {
    logger.warn("Validation failed for request: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              logger.warn("Validation error - Field '{}' - {}", fieldName, errorMessage);
              errors.put(fieldName, errorMessage);
            });

    String message = errors.isEmpty() ? "Validation failed" : errors.values().iterator().next();
    return ResponseEntity.badRequest().body(ApiResponse.error(message));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
    logger.warn("Bad credentials attempt: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Invalid username or password"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex) {
    logger.warn("Access denied: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ApiResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
    logger.warn("Rate limit exceeded, retry after {} seconds", ex.getWaitForRefill());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(ex.getWaitForRefill()))
        .body(
            ApiResponse.error(
                "Сервер перегружен запросами. Попробуйте через "
                    + ex.getWaitForRefill()
                    + " секунд."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
    logger.error("Unexpected error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("Internal server error"));
  }
}
