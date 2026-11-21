package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.ImageCompressionLevel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageCompressorRequestDto {
    @NotNull
    private Integer toolId;
    @NotNull
    private Integer subToolId;
    @NotNull(message = "Image must be provided")
    private MultipartFile image;
    @NotNull(message = "Image must be provided")
    @Enumerated(EnumType.STRING)
    private ImageCompressionLevel level;
}
