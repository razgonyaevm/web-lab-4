package com.example.backend.dto;

public record ApiResponse(String message, Object data) {

  public static ApiResponse success(String message) {
    return new ApiResponse(message, null);
  }

  public static ApiResponse success(String message, Object data) {
    return new ApiResponse(message, data);
  }

  public static ApiResponse error(String message) {
    return new ApiResponse(message, null);
  }
}
