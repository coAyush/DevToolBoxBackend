package com.devtoolbox.DevToolBoxRevamp.authentication.event;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.RefreshTokenRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenEventListener {

    private final RefreshTokenRepository refreshTokenRepository;
    @EventListener
    public void saveRefreshToken(RefreshTokenEvent refreshTokenEvent) {
        Instant expires=Instant.now().plus(7, TimeUnit.DAYS.toChronoUnit());
        RefreshToken refreshToken=RefreshToken.builder()
                .user(refreshTokenEvent.getUser())
                .duration(expires)
                .token(refreshTokenEvent.getRefreshToken())
                .build();
        RefreshToken savedToken=refreshTokenRepository.save(refreshToken);
        if(savedToken!=null){
            log.info("RefreshToken saved successfully");
        }
    }

}
