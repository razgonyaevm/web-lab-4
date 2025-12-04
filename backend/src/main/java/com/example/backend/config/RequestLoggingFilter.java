package com.example.backend.config;

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

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String method = exchange.getRequest().getMethod().name();
    String path = exchange.getRequest().getPath().value();

    return chain
        .filter(exchange)
        .doOnSuccess(
            v ->
                System.out.println(
                    method + " " + path + " - " + exchange.getResponse().getStatusCode()))
        .doOnError(
            e -> System.err.println("ERROR: " + method + " " + path + " - " + e.getMessage()));
  }
}
