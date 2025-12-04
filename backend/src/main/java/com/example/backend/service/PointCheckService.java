package com.example.backend.service;

import com.example.backend.entity.PointCheck;
import com.example.backend.entity.User;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PointCheckService {

  public Mono<PointCheck> checkPoint(User user, Integer x, Double y, Integer r) {
    return Mono.fromCallable(
        () -> {
          long startTime = System.nanoTime();

          boolean result = checkArea(x.doubleValue(), y, r.doubleValue());

          long endTime = System.nanoTime();

          long executionTime = (endTime - startTime) / 100;

          return new PointCheck(
              user.getId(),
              x.doubleValue(),
              y,
              r.doubleValue(),
              result,
              LocalDateTime.now(),
              executionTime);
        });
  }

  private boolean checkArea(double x, double y, double r) {

    // Четверть круга в первой четверти (радиус r/2)
    boolean inCircle = (x >= 0 && y >= 0) && (x * x + y * y <= (r / 2) * (r / 2));

    // Квадрат во второй четверти
    boolean inSquare = (x <= 0 && y >= 0) && (x >= -r && y <= r);

    // Треугольник в четвертой четверти
    boolean inTriangle = (x >= 0 && y <= 0) && (y >= x - r) && (x <= r) && (y >= -r);

    return inCircle || inSquare || inTriangle;
  }
}
