package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.CompressionLevels;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PdfCompressorRequestDto {

    @NotNull
    private MultipartFile file;
    @NotNull
    @Builder.Default
    private CompressionLevels compressionLevel=CompressionLevels.MEDIUM;

    @NotNull
    private Integer toolId;
    @NotNull
    private Integer subToolId;
}
