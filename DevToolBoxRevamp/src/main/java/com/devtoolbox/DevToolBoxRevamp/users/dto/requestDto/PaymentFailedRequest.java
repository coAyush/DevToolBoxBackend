package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotBlank;

public record PaymentFailedRequest(@NotBlank String orderId, String reason) {}