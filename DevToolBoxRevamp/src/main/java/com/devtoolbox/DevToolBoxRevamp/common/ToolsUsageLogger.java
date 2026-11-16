package com.devtoolbox.DevToolBoxRevamp.common;

import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ToolsUsageLogger {
    private final ToolUsageRepository toolUsageRepository;

    public ToolUsage logUsage(UUID userId,Integer toolId,Integer toolSubId){
        ToolUsage usage=ToolUsage.builder()
                .userId(userId)
                .toolId(toolId)
                .toolSubId(toolSubId)
                .build();
        ToolUsage saved=toolUsageRepository.save(usage);
        return saved;
    }
}
