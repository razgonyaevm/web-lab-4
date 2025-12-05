package com.example.backend.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimitAspect {

  @Autowired private RateLimitConfig rateLimitConfig;

  @Value("${rate-limit.enabled:false}")
  private boolean rateLimitEnabled;

  @Around("@annotation(com.example.backend.config.RateLimited)")
  public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
    // Skip rate limiting if disabled
    if (!rateLimitEnabled) {
      return joinPoint.proceed();
    }

    // Check rate limit BEFORE processing
    Bucket bucket = rateLimitConfig.getBucket();
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (!probe.isConsumed()) {
      // Rate limit exceeded - throw exception
      long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000; // Convert to seconds
      throw new RateLimitExceededException(waitForRefill);
    }

    // Request allowed - proceed normally
    return joinPoint.proceed();
  }
}
