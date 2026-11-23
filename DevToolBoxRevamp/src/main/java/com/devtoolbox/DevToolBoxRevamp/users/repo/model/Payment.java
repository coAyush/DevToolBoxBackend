package com.devtoolbox.DevToolBoxRevamp.users.repo.model;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "payment")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "package_id")
    private Integer packageId;
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "payment_id")
    private String paymentId;
    private String signature;
    @Column(name = "amount_paise")
    private Long amountPaise;
    private String currency;
    private String status;
    @Column(name = "failure_reason")
    private String failureReason;
}