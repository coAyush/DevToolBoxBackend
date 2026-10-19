package com.devtoolbox.DevToolBoxRevamp.authentication.event;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.AuthAccountRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.RefreshTokenRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AccountType;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AuthAccount;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

        User user;

        if (authAccount != null) {
            user = authAccount.getUser();
        } else {
            user = User.builder()
                    .name(event.getName())
                    .email(event.getEmail())
                    .role(event.getRole().name())
                    .accountType(AccountType.AUTH)
                    .imageUrl(event.getPicture())
                    .isVerified(true)
                    .build();

            user = userRepository.save(user);

            AuthAccount newAuthAccount = AuthAccount.builder()
                    .user(user)
                    .provider(event.getProvider())
                    .providerId(event.getProviderId())
                    .build();

            authAccountRepository.save(newAuthAccount);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String redisKey = REDIS_KEY + user.getId();
        String refreshToken = jwtService.generateRefreshToken(user);
        stringRedisTemplate.opsForValue().set(redisKey, accessToken, token_access_timeout);
        RefreshTokenEvent refreshTokenEvent = RefreshTokenEvent.builder()
                .user(user)
                .refreshToken(refreshToken)
                .build();

        eventPublisher.publishEvent(refreshTokenEvent);
        event.getTokens().put("accessToken", accessToken);
        event.getTokens().put("refreshToken", refreshToken);
    }

}

