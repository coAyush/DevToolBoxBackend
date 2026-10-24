package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboardResponseDto {
    private UserDashboardDetailsDto userDetails;
    private UserMonthlyStatsDto userMonthlyStatsDto;
}
