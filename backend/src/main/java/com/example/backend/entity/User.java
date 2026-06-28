package com.example.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(@Id Long id, String username, String password) {
  public User(String username, String password) {
    this(null, username, password);
  }
}
