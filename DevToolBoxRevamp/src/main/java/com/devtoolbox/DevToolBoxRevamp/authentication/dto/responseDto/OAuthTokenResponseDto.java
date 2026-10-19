package com.devtoolbox.DevToolBoxRevamp.authentication.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthTokenResponseDto {
    private String accessToken;
    private String refreshToken;
}