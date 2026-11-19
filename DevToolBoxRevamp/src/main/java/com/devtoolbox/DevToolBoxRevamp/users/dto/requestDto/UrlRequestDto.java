package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlRequestDto {
    @NotBlank(message = "url must be provided")
    private String longUrl;
    private String alias;
}
