package com.example.backend.config;

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
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      WebExchangeBindException ex) {
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

    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put(
        "message", errors.isEmpty() ? "Validation failed" : errors.values().iterator().next());
    response.put("errors", errors);

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
    logger.warn("Bad credentials attempt: {}", ex.getMessage());
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "Invalid username or password");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    logger.warn("Access denied: {}", ex.getMessage());
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "Access denied");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(
      RateLimitExceededException ex) {
    logger.warn("Rate limit exceeded, retry after {} seconds", ex.getWaitForRefill());
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put(
        "message",
        "Сервер перегружен запросами. Попробуйте через " + ex.getWaitForRefill() + " секунд.");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(ex.getWaitForRefill()))
        .body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    logger.error("Unexpected error occurred", ex);
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "Internal server error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
