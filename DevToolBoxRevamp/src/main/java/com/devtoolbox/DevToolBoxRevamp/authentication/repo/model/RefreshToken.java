package com.devtoolbox.DevToolBoxRevamp.authentication.repo.model;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_refresh_token_user_id")
    )
    private User user;
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;
    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;
    @Column(name = "duration", nullable = false)
    private Instant duration;

}
