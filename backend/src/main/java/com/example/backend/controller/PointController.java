package com.example.backend.controller;

import com.example.backend.config.RateLimited;
import com.example.backend.dto.PointCheckWithUser;
import com.example.backend.dto.PointRequest;
import com.example.backend.entity.PointCheck;
import com.example.backend.repository.PointCheckRepository;
import com.example.backend.service.PointCheckService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
  public Mono<Map<String, Object>> getHistory(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int pageSize) {

    // Validate parameters
    final int validatedPage = Math.max(1, page);
    final int validatedPageSize = (pageSize <= 0 || pageSize > 1000) ? 50 : pageSize;
    final long offset = (validatedPage - 1L) * validatedPageSize;

    return pointCheckRepository
        .findAllWithUsernameOrderByCheckTimeDesc(validatedPageSize, offset)
        .collectList()
        .zipWith(pointCheckRepository.countAll())
        .map(
            tuple -> {
              List<PointCheckWithUser> data = tuple.getT1();
              Long totalCount = tuple.getT2();

              Map<String, Object> response = new HashMap<>();
              response.put("data", data);
              response.put("totalCount", totalCount);
              response.put("page", validatedPage);
              response.put("pageSize", validatedPageSize);
              response.put("totalPages", (int) Math.ceil((double) totalCount / validatedPageSize));

              return response;
            });
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
