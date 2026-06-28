package com.example.backend.config;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
  private final long waitForRefill;

  public RateLimitExceededException(long waitForRefill) {
    super("Rate limit exceeded. Try again in " + waitForRefill + " seconds.");
    this.waitForRefill = waitForRefill;
  }
}
