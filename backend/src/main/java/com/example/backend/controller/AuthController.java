package com.example.backend.controller;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.service.JwtService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtService jwtService;

  public AuthController(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public Mono<Map<String, Object>> login(@Valid @RequestBody AuthRequest request) {
    return userService
        .authenticate(request.username(), request.password())
        .map(
            user -> {
              String token = jwtService.generateToken(user.username());
              Map<String, Object> response = new HashMap<>();
              response.put("success", true);
              response.put("message", "Login successful");
              response.put("data", new AuthResponse(token, user.username()));
              return response;
            })
        .defaultIfEmpty(createErrorResponse("Invalid username or password"));
  }

  @PostMapping("/register")
  public Mono<Map<String, Object>> register(@Valid @RequestBody AuthRequest request) {
    return userService
        .register(request.username(), request.password())
        .map(user -> createSuccessResponse("Registration successful"))
        .defaultIfEmpty(createErrorResponse("Username already exists"));
  }

  private Map<String, Object> createSuccessResponse(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", message);
    return response;
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    return response;
  }
}
