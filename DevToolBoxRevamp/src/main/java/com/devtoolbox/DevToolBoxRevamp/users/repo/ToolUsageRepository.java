package com.devtoolbox.DevToolBoxRevamp.users.repo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.SubscriptionDetails;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ToolUsageRepository extends JpaRepository<ToolUsage, UUID> {

    @Query(value = """
            SELECT EXTRACT(DAY FROM created_at)as day,COUNT(*)
            FROM tools_usage
            WHERE user_id = :userId
            AND created_at >= :startDate
            AND created_at <= :endDate
            GROUP BY EXTRACT(DAY FROM created_at)
            ORDER BY day
            """,nativeQuery = true)
    List<Object []> findToolUsageByUserId(@Param("userId") UUID userId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    Boolean existsByUserId(UUID userId);

    @Query(value = """
            SELECT
                      u.id AS user_id,
                      u.role AS role,
                      s.id AS subscription_id,
                      s.tier AS tier,
                      s.end_date AS end_date,
                      COALESCE(t.total_usage, 0) AS usage_count
                  FROM users u
                  LEFT JOIN subscription s
                      ON s.user_id = u.id
                  LEFT JOIN (
                      SELECT
                          user_id,
                          COUNT(*) AS total_usage
                      FROM tools_usage
                      WHERE user_id = :userId
                      GROUP BY user_id
                  ) t
                      ON t.user_id = u.id
                  WHERE u.id = :userId
        """, nativeQuery = true)
    SubscriptionDetails getSubscriptionDetails(@Param("userId") UUID userId);
}
