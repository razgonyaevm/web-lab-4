package com.example.backend.config;

import com.example.backend.service.JwtService;
import com.example.backend.service.UserService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtService jwtService;
  private final UserService userService;

  public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    String method = exchange.getRequest().getMethod().name();

    // Skip authentication for auth endpoints and static resources
    if (path.startsWith("/api/auth/") || !path.startsWith("/api/")) {
      return chain.filter(exchange);
    }

    // Handle OPTIONS requests (CORS preflight)
    if ("OPTIONS".equals(method)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        String username = jwtService.extractUsername(token);

        if (username != null && jwtService.isTokenValid(token, username)) {
          return userService
              .findByUsername(username)
              .flatMap(
                  user -> {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.username(), null, null);

                    SecurityContext context = new SecurityContextImpl(authentication);
                    return chain
                        .filter(exchange)
                        .contextWrite(
                            ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                  })
              .switchIfEmpty(
                  Mono.fromRunnable(() -> {})
                      .then(
                          Mono.defer(
                              () -> {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                              })));
        }
      } catch (Exception e) {
        System.err.println("JWT processing error: " + e.getMessage());
      }
    }

    // Authentication failed
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
