package com.devtoolbox.DevToolBoxRevamp.authentication.scheduler;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Component
@Slf4j
public class RefreshTokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    @Scheduled(cron = "0 1 * * * *")
    public void refreshTokenScheduler() {
        log.info("RefreshToken scheduler started");
        int deletedTokens= refreshTokenRepository.deleteByDurationBefore(Instant.now());
        log.info("RefreshToken scheduler finished deleted tokens {}", deletedTokens);
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanRevokedTokens() {
        log.info("RefreshToken scheduler started");
        int deleted=refreshTokenRepository.deleteByIsRevokedTrue();
        log.info("RefreshToken scheduler finished deleted tokens {}", deleted);
    }

}
