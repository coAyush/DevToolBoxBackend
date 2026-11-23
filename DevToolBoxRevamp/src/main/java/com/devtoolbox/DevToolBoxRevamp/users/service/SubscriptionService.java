package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.UserTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public void insertSubscription(CurrentUserPrincipal user){
        Subscription subscription = Subscription.builder()
                .userId(user.getUserId())
                .tier(UserTier.FREE.name())
                .build();
        subscriptionRepository.save(subscription);
    }
    @Transactional
    public Instant activatePlan(UUID userId, String tier, int durationDays) {
        Subscription s = subscriptionRepository.findByUserId(userId);
        if (s == null) s = Subscription.builder().userId(userId).build();

        Instant now = Instant.now();
        boolean stillRunning = Boolean.TRUE.equals(s.getIsActive())
                && s.getEndDate() != null && s.getEndDate().isAfter(now);
        Instant base = stillRunning ? s.getEndDate() : now;
        s.setStartDate(stillRunning ? s.getStartDate() : now);
        Instant endDate=base.plus(durationDays, ChronoUnit.DAYS);
        s.setEndDate(endDate);
        s.setTier(tier);
        s.setIsActive(true);
        subscriptionRepository.save(s);
        return endDate;
    }
}
