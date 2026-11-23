package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record VerifyPaymentRequestDto(
    @NotBlank
    String razorpay_order_id,
    @NotBlank
    String razorpay_payment_id,
    @NotBlank
    String razorpay_signature){
}
