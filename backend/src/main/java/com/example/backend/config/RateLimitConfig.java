package com.example.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RateLimitConfig {

  private final Bucket bucket;

  public RateLimitConfig(
      @Value("${rate-limit.requests-per-minute:60}") int requestsPerMinute,
      @Value("${rate-limit.burst-capacity:100}") int burstCapacity) {
    this.bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    burstCapacity, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))))
            .build();
  }
}
