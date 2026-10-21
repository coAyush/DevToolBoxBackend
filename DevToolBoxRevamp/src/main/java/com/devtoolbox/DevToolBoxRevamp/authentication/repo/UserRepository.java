package com.devtoolbox.DevToolBoxRevamp.authentication.repo;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(@NotNull @Email String email);

    User findByEmail(String email);

    @Modifying
    @Query(value= """
            UPDATE users
            SET is_verified=true
            WHERE id= :userId
            """,nativeQuery = true)
    int updateIsVerifiedByiD(@Param("userId") UUID userId);

    @Modifying
    @Query(value = """
    INSERT INTO users (
        id, name, email, phone, password, role,
        is_verified, account_type, image_url,
        api_key, created_at, updated_at
    )
    VALUES (
        :id, :name, :email, :phone, :password, :role,
        :isVerified, :accountType, :imageUrl,
        :apiKey, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
    ON CONFLICT (email)
    DO UPDATE SET
        name = EXCLUDED.name,
        image_url = EXCLUDED.image_url,
        updated_at = CURRENT_TIMESTAMP;
    """, nativeQuery = true)
    int upsertUser(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("password") String password,
            @Param("role") String role,
            @Param("isVerified") boolean isVerified,
            @Param("accountType") String accountType,
            @Param("imageUrl") String imageUrl,
            @Param("apiKey") String apiKey
    );

}
