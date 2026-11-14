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
public class QrCodeRequestDto {
    @NotNull
    private int toolId;
    @NotNull
    private int toolSubId;
    @Builder.Default
    private Integer width=300;
    @Builder.Default
    private Integer height=300;
    @NotBlank(message = "url field must be provided")
    private String url;

    public int getWidth() {
        return width != null ? width : 300;
    }

    public int getHeight() {
        return height != null ? height : 300;
    }
}
