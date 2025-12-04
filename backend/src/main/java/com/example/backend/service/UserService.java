package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Mono<User> register(String username, String plainPassword) {
    return userRepository
        .existsByUsername(username)
        .flatMap(
            exists -> {
              if (exists) {
                return Mono.empty(); // Если пользователь уже существует
              }
              User user = new User(username, passwordEncoder.encode(plainPassword));
              return userRepository.save(user);
            });
  }

  public Mono<User> authenticate(String username, String plainPassword) {
    return userRepository
        .findByUsername(username)
        .filter(user -> passwordEncoder.matches(plainPassword, user.getPassword()));
  }

  public Mono<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
