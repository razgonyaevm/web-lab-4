package com.example.backend.dto;

import java.time.LocalDateTime;

public class PointCheckWithUser {
  private Long id;
  private String username;
  private Double x;
  private Double y;
  private Double r;
  private Boolean result;
  private LocalDateTime checkTime;
  private Long executionTime;

  public PointCheckWithUser() {}

  public PointCheckWithUser(
      Long id,
      String username,
      Double x,
      Double y,
      Double r,
      Boolean result,
      LocalDateTime checkTime,
      Long executionTime) {
    this.id = id;
    this.username = username;
    this.x = x;
    this.y = y;
    this.r = r;
    this.result = result;
    this.checkTime = checkTime;
    this.executionTime = executionTime;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Double getX() {
    return x;
  }

  public void setX(Double x) {
    this.x = x;
  }

  public Double getY() {
    return y;
  }

  public void setY(Double y) {
    this.y = y;
  }

  public Double getR() {
    return r;
  }

  public void setR(Double r) {
    this.r = r;
  }

  public Boolean getResult() {
    return result;
  }

  public void setResult(Boolean result) {
    this.result = result;
  }

  public LocalDateTime getCheckTime() {
    return checkTime;
  }

  public void setCheckTime(LocalDateTime checkTime) {
    this.checkTime = checkTime;
  }

  public Long getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(Long executionTime) {
    this.executionTime = executionTime;
  }
}
