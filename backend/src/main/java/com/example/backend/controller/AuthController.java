package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.service.JwtService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public Mono<ResponseEntity<ApiResponse>> login(@Valid @RequestBody AuthRequest request) {
    return userService
        .authenticate(request.username(), request.password())
        .map(
            user -> {
              String token = jwtService.generateToken(user.username());
              AuthResponse authData = new AuthResponse(token, user.username());
              return ResponseEntity.ok(ApiResponse.success("Login successful", authData));
            })
        .defaultIfEmpty(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password")));
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<ApiResponse>> register(@Valid @RequestBody AuthRequest request) {
    return userService
        .register(request.username(), request.password())
        .map(
            user ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful")))
        .defaultIfEmpty(
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Username already exists")));
  }
}
