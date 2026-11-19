package com.devtoolbox.DevToolBoxRevamp.users.repo.model;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="url")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Url {
    @Id
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                    // apni User entity

    @Column(name = "is_custom_alias", nullable = false)
    private boolean customAlias;

    @Column(name = "click_count", nullable = false)
    private long clickCount;

    @Column(name = "last_clicked_at")
    private Instant lastClickedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
