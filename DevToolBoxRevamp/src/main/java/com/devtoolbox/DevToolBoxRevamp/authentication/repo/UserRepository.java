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
}
