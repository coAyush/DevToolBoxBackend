package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto.ClickSnapshotDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.UrlBatchPersistenceRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlClickPersistenceService {
    private final UrlRepository urlRepository;
    private final UrlBatchPersistenceRepository batchRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisClickSnapShotService redisClickSnapShotService;
    private final TransactionTemplate transactionTemplate;
    public void preserveUrlClicks(Long urlId) {
        Set<String> processingKeys = stringRedisTemplate.keys("url-clicks:" + urlId + ":processing:*");
        if (processingKeys != null) {
            for (String key : processingKeys) {
                processSnapshot(urlId, key);
            }
        }

        String activeKey = "url-clicks:" + urlId;
        String clicks = stringRedisTemplate.opsForValue().get(activeKey);
        if (clicks == null || Long.parseLong(clicks) <= 0) {
            return;
        }
        String batchId = UUID.randomUUID().toString();
        redisClickSnapShotService.takeSnapshot(urlId, batchId);
        String processingKey = activeKey + ":processing:" + batchId;
        processSnapshot(urlId, processingKey);
    }

    private  void processSnapshot(Long urlId, String processingKey) {
        String[] parts = processingKey.split(":", 4);
        String batchId = parts[3];
        ClickSnapshotDto snapshot = redisClickSnapShotService.getProcessingSnapshot(processingKey);
        if (snapshot == null || snapshot.getClicks() == null
                || snapshot.getClicks() <= 0) {
            return;
        }
        transactionTemplate.executeWithoutResult(status -> {preserve(urlId, batchId, snapshot);
        });
        stringRedisTemplate.delete(processingKey);
    }
    public void preserve(Long urlId, String batchId,ClickSnapshotDto snapshot) {

        Instant lastClickedAt = snapshot.getLastClickedAt() != null ? Instant.parse(snapshot.getLastClickedAt()): null;
        int inserted = batchRepository.insertIfAbsent(UUID.randomUUID(), urlId, batchId, snapshot.getClicks(), lastClickedAt, Instant.now());
        if (inserted == 0) {
            return;
        }
        log.info(String.valueOf(snapshot.getClicks()));
        int updated = urlRepository.incrementClickCountAndLastClicked(snapshot.getClicks(), urlId, lastClickedAt);
        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not update URL: " + urlId
            );
        }
    }
}
