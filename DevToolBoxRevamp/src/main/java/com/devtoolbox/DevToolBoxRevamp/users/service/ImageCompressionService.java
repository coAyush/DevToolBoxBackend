package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.ImageCompressorRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ImageCompressionService {
    private final ToolUsageRepository   toolUsageRepository;

    @SneakyThrows
    public ResponseEntity<byte[]> compress(CurrentUserPrincipal user, ImageCompressorRequestDto dto){
        MultipartFile image = dto.getImage();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Double compressionLevel=dto.getLevel().getCompressionLevel();
        Thumbnails.of(image.getInputStream())
                .scale(1.0)
                .outputQuality(compressionLevel)
                .outputFormat("jpg")
                .toOutputStream(output);
        ToolUsage usage=ToolUsage.builder()
                .userId(user.getUserId())
                .toolId(dto.getToolId())
                .toolSubId(dto.getSubToolId())
                .build();
        toolUsageRepository.save(usage);
       return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="+image.getOriginalFilename()+"\"compressed.jpg\""
                )
                .contentType(MediaType.IMAGE_JPEG)
                .body(output.toByteArray());
    }
}
