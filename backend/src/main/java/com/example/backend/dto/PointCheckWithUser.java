package com.example.backend.dto;

import java.time.LocalDateTime;

public record PointCheckWithUser(
    Long id,
    String username,
    Double x,
    Double y,
    Double r,
    Boolean result,
    LocalDateTime checkTime,
    Long executionTime) {}
