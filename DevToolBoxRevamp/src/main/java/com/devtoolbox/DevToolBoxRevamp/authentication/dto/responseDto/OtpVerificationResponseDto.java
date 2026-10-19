package com.devtoolbox.DevToolBoxRevamp.authentication.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerificationResponseDto {
    String role;
}
