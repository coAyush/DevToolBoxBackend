package com.devtoolbox.DevToolBoxRevamp.users.repo;

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

}
