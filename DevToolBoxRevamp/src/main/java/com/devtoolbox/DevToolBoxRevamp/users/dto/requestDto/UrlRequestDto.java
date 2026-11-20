package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "toolId must not be null")
    private Integer toolId;
    @NotNull(message = "subToolId must not be null")
    private Integer subToolId;
}
