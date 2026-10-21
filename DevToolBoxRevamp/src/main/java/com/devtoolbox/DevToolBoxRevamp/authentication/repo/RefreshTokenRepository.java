package com.devtoolbox.DevToolBoxRevamp.authentication.repo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Override
    void deleteById(UUID uuid);

    @Modifying
    @Query(value = """
        UPDATE refresh_tokens
        SET is_revoked = false
        WHERE id = :id
        """, nativeQuery = true)
    int updateIsRevoked(@Param("id") UUID id);


    int deleteByDurationBefore(Instant time);

    int deleteByIsRevokedTrue();

    @Modifying
    @Query(value = """
    INSERT INTO refresh_tokens
        (user_id, token, is_revoked, duration, created_at, updated_at)
    VALUES
        (:userId, :token, FALSE, :duration, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id)
    DO UPDATE SET
        token = EXCLUDED.token,
        is_revoked = FALSE,
        duration = EXCLUDED.duration,
        updated_at = CURRENT_TIMESTAMP
    """, nativeQuery = true)
    int upsertRefreshToken(
            @Param("userId") UUID userId,
            @Param("token") String token,
            @Param("duration") Instant duration
    );
}
