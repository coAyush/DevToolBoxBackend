package com.devtoolbox.DevToolBoxRevamp.landing.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceResponseDto {
    Integer id;
    BigDecimal amount;
    String role;
    String tier;
    Integer duration;
    Boolean isActive;
}
