package com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedUrlDto {
    private Long id;
    private String originalUrl;
}