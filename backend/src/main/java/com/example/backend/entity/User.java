package com.example.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
@NoArgsConstructor
public class User {
  @Id private Long id;
  private String username;
  private String password;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
