
package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto.ClickSnapshotDto;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisClickSnapShotService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SNAPSHOT_SCRIPT = """
        local clicks = redis.call('GET', KEYS[1])

        if clicks and tonumber(clicks) > 0 then
            local lastClicked = redis.call('GET', KEYS[3])

            local snapshot = cjson.encode({
                clicks = tonumber(clicks),
                lastClickedAt = lastClicked
            })

            redis.call('SET', KEYS[2], snapshot)
            redis.call('SET', KEYS[1], 0)
            redis.call('DEL', KEYS[3])

            return snapshot
        end

        return cjson.encode({clicks = 0})
        """;

    @SneakyThrows
    public ClickSnapshotDto takeSnapshot(Long urlId, String batchId) {
        String activeKey = "url-clicks:" + urlId;
        String processingKey =
                "url-clicks:" + urlId + ":processing:" + batchId;
        String lastClickedKey = "url-last-clicked:" + urlId;
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(SNAPSHOT_SCRIPT);
        script.setResultType(String.class);
        String result = stringRedisTemplate.execute(
                script,
                List.of(activeKey, processingKey, lastClickedKey)
        );
        return objectMapper.readValue(result, ClickSnapshotDto.class);
    }

    @SneakyThrows
    public ClickSnapshotDto getProcessingSnapshot(String processingKey) {
        String data = stringRedisTemplate.opsForValue().get(processingKey);
        if (data == null) {
            return null;
        }
        return objectMapper.readValue(data, ClickSnapshotDto.class);
    }
}
