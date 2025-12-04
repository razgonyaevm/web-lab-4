package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.service.JwtService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
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
  public Mono<ApiResponse> login(@Valid @RequestBody AuthRequest request) {
    return userService
        .authenticate(request.getUsername(), request.getPassword())
        .map(
            user -> {
              String token = jwtService.generateToken(user.getUsername());
              return ApiResponse.success(
                  "Login successful", new AuthResponse(token, user.getUsername()));
            })
        .defaultIfEmpty(ApiResponse.error("Invalid username or password"));
  }

  @PostMapping("/register")
  public Mono<ApiResponse> register(@Valid @RequestBody AuthRequest request) {
    return userService
        .register(request.getUsername(), request.getPassword())
        .map(user -> ApiResponse.success("Registration successful"))
        .defaultIfEmpty(ApiResponse.error("Username already exists"));
  }
}
