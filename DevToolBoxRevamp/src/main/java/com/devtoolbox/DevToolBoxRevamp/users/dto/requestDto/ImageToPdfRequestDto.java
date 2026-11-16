package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.PageMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class ImageToPdfRequestDto {
    @NotNull
    Integer toolId;
    @NotNull
    Integer subToolId;
    @NotEmpty(message = "At least one image is required")
    @Size(max = 20, message = "Maximum 20 images allowed")
    private List<MultipartFile> files;

    @Builder.Default
    private PageMode pageMode = PageMode.FIT_IMAGE;
}
