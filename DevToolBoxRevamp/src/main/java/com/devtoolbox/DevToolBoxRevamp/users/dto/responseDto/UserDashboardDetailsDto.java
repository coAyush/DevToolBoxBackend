package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDashboardDetailsDto {
    private String name;
    private String imageUrl;
    private Boolean isActive;
    private Boolean subscriptionStatus;
}
