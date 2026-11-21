package com.devtoolbox.DevToolBoxRevamp.authentication.repo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public interface SubscriptionDetails {
    UUID getUserId();

    String getRole();

    UUID  getSubscriptionId();

    String getTier();

    Instant getEndDate();

    Long getUsageCount();
}
