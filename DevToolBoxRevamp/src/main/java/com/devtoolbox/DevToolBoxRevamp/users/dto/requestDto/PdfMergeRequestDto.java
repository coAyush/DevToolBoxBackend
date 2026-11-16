package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PdfMergeRequestDto {
    @NotNull
    private Integer toolId;
    @NotNull
    private Integer subToolId;
    @NotNull(message = "Files are required")
    @Size(min = 2, max = 20, message = "Upload between 2 and 20 PDFs")
    private List<MultipartFile> files;
}
