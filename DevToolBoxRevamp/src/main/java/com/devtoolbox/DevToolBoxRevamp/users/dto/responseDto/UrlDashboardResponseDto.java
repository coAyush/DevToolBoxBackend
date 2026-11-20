package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlDashboardResponseDto {
    private Long id;
    private String shortUrl;
    private String originalUrl;
    private Long clickCount;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant lastClickedAt;
}
