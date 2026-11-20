package com.devtoolbox.DevToolBoxRevamp.scheduler;

import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.UrlBatchPersistenceRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.UrlRepository;
import com.devtoolbox.DevToolBoxRevamp.users.service.RedisClickSnapShotService;
import com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto.ClickSnapshotDto;
import com.devtoolbox.DevToolBoxRevamp.users.service.UrlClickPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class UrlClicksPreserverScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisClickSnapShotService redisClickSnapShotService;
    private final UrlRepository urlRepository;
    private final UrlBatchPersistenceRepository urlBatchRepo;
    private final TransactionTemplate transactionTemplate;
    private final UrlClickPersistenceService urlClickPersistenceService;
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void preserveClicks() {
        log.info("Preserve clicks");
        Set<String> keys = stringRedisTemplate.keys("url-clicks:*");
        if (keys != null) {
            for (String key : keys) {
                if (key.contains(":processing:")) {
                    continue;
                }
                try {
                    Long urlId = Long.valueOf(
                            key.substring("url-clicks:".length())
                    );
                    String batchId = UUID.randomUUID().toString();
                    ClickSnapshotDto redisSnap=redisClickSnapShotService.takeSnapshot(urlId, batchId);
                } catch (Exception e) {
                    log.error("Snapshot failed for key: {}", key, e);
                }
            }
        }
        Set<String> processingKeys =stringRedisTemplate.keys("url-clicks:*:processing:*");
        if (processingKeys == null || processingKeys.isEmpty()) {
            return;
        }
        for (String processingKey : processingKeys) {
            try {
                String[] parts = processingKey.split(":", 4);
                Long urlId = Long.valueOf(parts[1]);
                String batchId = parts[3];
                ClickSnapshotDto snapshot = redisClickSnapShotService.getProcessingSnapshot(processingKey);
                if (snapshot == null || snapshot.getClicks() == null
                        || snapshot.getClicks() <= 0) {
                    continue;
                }
                Instant lastClickedAt =
                        snapshot.getLastClickedAt() != null
                                ? Instant.parse(snapshot.getLastClickedAt())
                                : null;

                transactionTemplate.executeWithoutResult(status->urlClickPersistenceService.preserve(urlId,batchId,snapshot));
                stringRedisTemplate.delete(processingKey);
                log.info("Persisted URL ID: {}, clicks: {}",
                        urlId, snapshot.getClicks());
            } catch (Exception e) {
                // Keep the processing key for retry on the next execution.
                log.error("Failed to persist snapshot: {}", processingKey, e);
            }
        }
    }
}