package com.devtoolbox.DevToolBoxRevamp.users.repo.model;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "tier", length = 100)
    private String tier;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;
}