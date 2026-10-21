package com.devtoolbox.DevToolBoxRevamp.authentication.repo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface AuthAccountRepository extends JpaRepository<AuthAccount, String> {
    Optional<AuthAccount> findByProviderAndProviderId(String provider, String providerId);
        @Modifying
        @Query(value = """
        INSERT INTO auth_account (
            id,
            user_id,
            provider,
            provider_id,
            created_at,
            updated_at
        )
        VALUES (
            :id,
            :userId,
            :provider,
            :providerId,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (provider_id)
        DO UPDATE SET
            provider = EXCLUDED.provider,
            updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
        int upsertAuthAccount(
                @Param("id") UUID id,
                @Param("userId") UUID userId,
                @Param("provider") String provider,
                @Param("providerId") String providerId
        );
}

