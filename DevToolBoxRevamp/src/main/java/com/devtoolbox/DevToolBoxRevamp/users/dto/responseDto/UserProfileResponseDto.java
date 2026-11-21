package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponseDto {

    // User details
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;

    // Account details
    private String role;
    private String loginProvider;
    private boolean emailVerified;
    private Instant createdAt;

    // Subscription details
    private String subscriptionTier;
    private Boolean subscriptionStatus;
    private Instant subscriptionStartDate;
    private Instant subscriptionEndDate;

    // Usage details
    private long monthlyUsage;
    private long usageLimit;
    private long remainingUsage;
}