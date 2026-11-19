package com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.UrlClickBatchPersistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface UrlBatchPersistenceRepository extends JpaRepository<UrlClickBatchPersistence, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO url_click_batch_persistence
        (id, url_id, batch_id, clicks, last_clicked_at, created_at, updated_at)
        VALUES
        (:id, :urlId, :batchId, :clicks, :lastClickedAt, :now, :now)
        ON CONFLICT (batch_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("urlId") Long urlId,
            @Param("batchId") String batchId,
            @Param("clicks") Long clicks,
            @Param("lastClickedAt") Instant lastClickedAt,
            @Param("now") Instant now
    );}
