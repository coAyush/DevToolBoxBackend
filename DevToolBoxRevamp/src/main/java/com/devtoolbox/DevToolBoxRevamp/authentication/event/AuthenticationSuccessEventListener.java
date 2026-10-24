package com.devtoolbox.DevToolBoxRevamp.authentication.event;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.AuthAccountRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.RefreshTokenRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AccountType;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AuthAccount;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.security.JwtService;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.UserTier;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthenticationSuccessEventListener {
    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String REDIS_KEY = "ACCESS_TOKEN:user_id:";
    private final SubscriptionRepository subscriptionRepository;
    @Value("${token.access-timeout}")
    private Duration token_access_timeout;

    @EventListener
    @Transactional
    public void onAuthenticationSuccessEvent(OAuthUserAuthenticatedEvent event) {
        log.info("Inside AuthenticationSuccessEventListener");

        log.info("Inside AuthenticationSuccessEventListener");

        AuthAccount authAccount = authAccountRepository
                .findByProviderAndProviderId(
                        event.getProvider(),
                        event.getProviderId()
                )
                .orElse(null);

        User savedUser = null;
        if (authAccount != null) {
            savedUser = authAccount.getUser();
        } else {
            int newUser = userRepository.upsertUser(UUID.randomUUID(),
                    event.getName(),
                    event.getEmail(),
                    null,                    // phone
                    null,                    // password
                    event.getRole().name(),
                    true,
                    AccountType.AUTH.name(),
                    event.getPicture(),
                    null);
            if (newUser <= 0) {
                throw new RuntimeException("User could not be logged in");
            }
             savedUser = userRepository.findByEmail(event.getEmail());
            if(!subscriptionRepository.existsByUserId(savedUser.getId())){
                Subscription subscription = Subscription.builder()
                        .userId(savedUser.getId())
                        .tier(UserTier.FREE.name())
                        .isActive(false)
                        .build();
                subscriptionRepository.save(subscription);
            }
            AuthAccount newAuthAccount = AuthAccount.builder()
                    .user(savedUser)
                    .provider(event.getProvider())
                    .providerId(event.getProviderId())
                    .build();
            UUID authId = UUID.randomUUID();
            int flag2 = authAccountRepository.upsertAuthAccount(authId, savedUser.getId(), newAuthAccount.getProvider(), newAuthAccount.getProviderId());
            if (flag2 <= 0) {
                throw new RuntimeException("User could not be logged in");
            }

        }

        String accessToken = jwtService.generateAccessToken(savedUser);
        String redisKey = REDIS_KEY + savedUser.getId();
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        stringRedisTemplate.opsForValue().set(redisKey, accessToken, token_access_timeout);
        RefreshTokenEvent refreshTokenEvent = RefreshTokenEvent.builder()
                .user(savedUser)
                .refreshToken(refreshToken)
                .build();

        eventPublisher.publishEvent(refreshTokenEvent);
        event.getTokens().put("accessToken", accessToken);
        event.getTokens().put("refreshToken", refreshToken);
    }

}

