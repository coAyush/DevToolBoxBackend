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
}
