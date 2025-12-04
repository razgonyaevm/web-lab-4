package com.example.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

  @Value("${rate-limit.requests-per-minute:60}")
  private int requestsPerMinute;

  @Value("${rate-limit.burst-capacity:100}")
  private int burstCapacity;

  // Store bucket in memory for single instance
  private volatile Bucket bucket;

  public Bucket getBucket() {
    if (bucket == null) {
      synchronized (this) {
        if (bucket == null) {
          bucket =
              Bucket.builder()
                  .addLimit(
                      Bandwidth.classic(
                          burstCapacity,
                          Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))))
                  .build();
        }
      }
    }
    return bucket;
  }
}
