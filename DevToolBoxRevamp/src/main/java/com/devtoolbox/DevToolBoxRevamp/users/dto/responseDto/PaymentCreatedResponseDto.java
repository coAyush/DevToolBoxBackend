package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreatedResponseDto {
    private String orderId;
    private String paymentId;
    private Long amount;
    private String packagePurchased;
}
