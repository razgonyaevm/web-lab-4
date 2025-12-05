package com.example.backend.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("point_checks")
public record PointCheck(
    @Id Long id,
    Long userId,
    Double x,
    Double y,
    Double r,
    Boolean result,
    LocalDateTime checkTime,
    Long executionTime) {
  public PointCheck(
      Long userId,
      Double x,
      Double y,
      Double r,
      Boolean result,
      LocalDateTime checkTime,
      Long executionTime) {
    this(null, userId, x, y, r, result, checkTime, executionTime);
  }
}
