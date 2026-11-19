package com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickSnapshotDto {
    private Long clicks;
    private String lastClickedAt;
}

