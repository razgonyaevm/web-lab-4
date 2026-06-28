package com.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestLoggingFilter implements WebFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String method = exchange.getRequest().getMethod().name();
    String path = exchange.getRequest().getPath().value();

    return chain
        .filter(exchange)
        .doOnSuccess(
            v -> logger.info("{} {} - {}", method, path, exchange.getResponse().getStatusCode()))
        .doOnError(e -> logger.error("Request failed: {} {} - {}", method, path, e.getMessage()));
  }
}
