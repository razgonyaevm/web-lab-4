package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PointRequest {
  @NotNull(message = "X coordinate is required")
  @Min(value = -3, message = "X must be >= -3")
  @Max(value = 5, message = "X must be <= 5")
  private Integer x;

  @NotNull(message = "Y coordinate is required")
  @DecimalMin(value = "-3", message = "Y must be >= -3")
  @DecimalMax(value = "3", message = "Y must be <= 3")
  private Double y;

  @NotNull(message = "Radius is required")
  @Min(value = 1, message = "R must be >= 1")
  @Max(value = 5, message = "R must be <= 5")
  private Integer r;
}
