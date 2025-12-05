package com.example.backend.controller;

import com.example.backend.config.RateLimited;
import com.example.backend.dto.PointCheckWithUser;
import com.example.backend.dto.PointRequest;
import com.example.backend.entity.PointCheck;
import com.example.backend.repository.PointCheckRepository;
import com.example.backend.service.PointCheckService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/points")
public class PointController {

  private final PointCheckService pointCheckService;
  private final PointCheckRepository pointCheckRepository;
  private final UserService userService;

  public PointController(
      PointCheckService pointCheckService,
      PointCheckRepository pointCheckRepository,
      UserService userService) {
    this.pointCheckRepository = pointCheckRepository;
    this.pointCheckService = pointCheckService;
    this.userService = userService;
  }

  @PostMapping("/check")
  @RateLimited
  public Mono<PointCheck> checkPoint(
      @Valid @RequestBody PointRequest request, Authentication authentication) {
    String username = authentication.getName();

    return userService
        .findByUsername(username)
        .flatMap(
            user ->
                pointCheckService.checkPoint(user, request.getX(), request.getY(), request.getR()))
        .flatMap(pointCheck -> pointCheckRepository.save(pointCheck));
  }

  @GetMapping("/history")
  @RateLimited
  public Flux<PointCheckWithUser> getHistory(
      @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") long offset) {

    // Validate parameters
    if (limit <= 0 || limit > 1000) {
      limit = 50; // Default limit
    }
    if (offset < 0) {
      offset = 0;
    }

    return pointCheckRepository.findAllWithUsernameOrderByCheckTimeDesc(limit, offset);
  }

  @GetMapping("/history/count")
  @RateLimited
  public Mono<Long> getHistoryCount() {
    return pointCheckRepository.countAll();
  }

  @DeleteMapping("/history")
  @RateLimited
  public Mono<ResponseEntity<String>> clearHistory(Authentication authentication) {
    String username = authentication.getName();

    return userService
        .findByUsername(username)
        .flatMap(
            user ->
                pointCheckRepository
                    .findByUserId(user.id())
                    .collectList()
                    .flatMap(
                        pointChecks -> {
                          if (pointChecks.isEmpty()) {
                            return Mono.just(pointChecks);
                          }
                          return pointCheckRepository
                              .deleteAll(pointChecks)
                              .then(Mono.just(pointChecks));
                        }))
        .map(deletedItems -> ResponseEntity.ok("History cleared successfully"))
        .defaultIfEmpty(ResponseEntity.status(404).body("User not found"))
        .onErrorResume(
            error -> Mono.just(ResponseEntity.status(500).body("Internal server error")));
  }
}
