package com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, Long> {

    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long nextId();

    Optional<Url> findByShortCodeAndDeletedAtIsNull(String shortCode);
    Optional<Url> findByUserAndOriginalUrlAndDeletedAtIsNull(User u, String originalUrl);


    @Modifying
    @Query(value = """
    UPDATE url
    SET click_count = click_count + :clicks,
        last_clicked_at = CASE
             WHEN CAST(:lastClickedAt AS TIMESTAMP WITH TIME ZONE) IS NULL THEN last_clicked_at
            ELSE GREATEST(last_clicked_at, :lastClickedAt)
        END
    WHERE id = :id
    """, nativeQuery = true)
    int incrementClickCountAndLastClicked(
            @Param("clicks") Long clicks,
            @Param("id") Long id,
            @Param("lastClickedAt") Instant lastClickedAt
    );

    List<Url> findAllByUserId(UUID userId);
}