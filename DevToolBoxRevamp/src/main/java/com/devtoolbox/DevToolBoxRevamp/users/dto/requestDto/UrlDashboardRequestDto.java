package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UrlDashboardRequestDto {
    private Integer toolId;
    private Integer subToolId;
}
