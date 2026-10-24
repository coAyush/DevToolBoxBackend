package com.devtoolbox.DevToolBoxRevamp.users.repo;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Subscription findByUserId(UUID userId);

    boolean existsByUserId(UUID id);
}
