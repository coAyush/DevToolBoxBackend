package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.common.ToolsUsageLogger;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.PdfCompressorRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.CompressionLevels;
import com.devtoolbox.DevToolBoxRevamp.util.GhostScriptService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfCompressorService {
    private final ToolsUsageLogger toolsUsageLogger;
    private final GhostScriptService ghostScriptService;

    @SneakyThrows
    public ResponseEntity<byte[]> compressedPdf(CurrentUserPrincipal user, PdfCompressorRequestDto dtoObject) {
        Integer toolId=dtoObject.getToolId();
        Integer subToolId=dtoObject.getSubToolId();
        CompressionLevels compressionLevels=dtoObject.getCompressionLevel();
        MultipartFile originalPdf= dtoObject.getFile();
        ToolUsage saved=toolsUsageLogger.logUsage(user.userId,toolId,subToolId);
        if(saved==null){
            log.error("ToolUsage could not be saved for user {}",user.userId);
            throw new RuntimeException("ToolUsage could not be saved for user "+user.userId);
        }
        byte[] compressedFile=ghostScriptService.compressedPdf(originalPdf,compressionLevels);
        if(compressedFile.length==0){
            log.error("failed to generate");
            throw new RuntimeException("failed to generate");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+originalPdf.getOriginalFilename()+"compressed.pdf")
                .body(compressedFile);
    }
}
