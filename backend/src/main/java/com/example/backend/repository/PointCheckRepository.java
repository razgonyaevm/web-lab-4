package com.example.backend.repository;

import com.example.backend.dto.PointCheckWithUser;
import com.example.backend.entity.PointCheck;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PointCheckRepository extends R2dbcRepository<PointCheck, Long> {

  // Legacy method for individual user history (might still be needed)
  @Query("SELECT * FROM point_checks WHERE user_id = :userId ORDER BY check_time DESC")
  Flux<PointCheck> findByUserIdOrderByCheckTimeDesc(Long userId);

  // Get all points from all users with username, sorted by time, with pagination
  @Query(
      """
    SELECT pc.id, u.username, pc.x, pc.y, pc.r, pc.result, pc.check_time, pc.execution_time
    FROM point_checks pc
    JOIN users u ON pc.user_id = u.id
    ORDER BY pc.check_time DESC
    LIMIT :limit OFFSET :offset
    """)
  Flux<PointCheckWithUser> findAllWithUsernameOrderByCheckTimeDesc(int limit, long offset);

  // Get total count for pagination
  @Query("SELECT COUNT(*) FROM point_checks")
  Mono<Long> countAll();

  Flux<PointCheck> findByUserId(Long userId);
}
