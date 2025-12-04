package com.example.backend.controller;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

  @GetMapping(value = "/api/auth/health", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, String>> health() {
    return Mono.just(
        Map.of(
            "status", "OK",
            "message", "Backend is running",
            "timestamp", java.time.LocalDateTime.now().toString()));
  }

  @GetMapping("/api/test")
  public Mono<Map<String, String>> test() {
    return Mono.just(Map.of("test", "API is working"));
  }
}
