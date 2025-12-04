package com.example.backend.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("point_checks")
public class PointCheck {
  @Id private Long id;
  private Long userId;
  private Double x;
  private Double y;
  private Double r;
  private Boolean result;
  private LocalDateTime checkTime;
  private Long executionTime;

  public PointCheck(
      Long userId,
      Double x,
      Double y,
      Double r,
      Boolean result,
      LocalDateTime checkTime,
      Long executionTime) {
    this.userId = userId;
    this.x = x;
    this.y = y;
    this.r = r;
    this.result = result;
    this.checkTime = checkTime;
    this.executionTime = executionTime;
  }
}
