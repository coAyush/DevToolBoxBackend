package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordToPdfRequestDto {
    @NotNull
    private Integer toolId;
    @NotNull
    private Integer subToolId;
    @NotNull
    private MultipartFile file;
}
