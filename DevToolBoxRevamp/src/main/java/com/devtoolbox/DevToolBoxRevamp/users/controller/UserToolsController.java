package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUser;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.*;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.QrCode;
import com.devtoolbox.DevToolBoxRevamp.users.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/users/tools")
@RestController
@RequiredArgsConstructor
@Tag(name = "user tools",description = "this contains all the tools for users")
public class UserToolsController {

    private final QrCodeService qrCodeService;
    private final PdfCompressorService pdfCompressorService;
    private final ImageToPdfService imageToPdfService;
    private final WordToPdfService wordToPdfService;
    private final PdfMergeService pdfMergeService;

    @Operation(description = "qr code generator", summary = "genrates the qr code of any url with this endpoint")
    @PostMapping("/qr-code")
    public ResponseEntity<ApiResponse> qrCodeGenerator(@CurrentUser CurrentUserPrincipal user, @Valid @RequestBody QrCodeRequestDto qrCodeDto) {
        return ResponseEntity.ok(qrCodeService.generateQr(user, qrCodeDto));
    }

    @Operation(description = "pdf compressor", summary = "compresses the pdf code with levels specified")
    @PostMapping(value = "/pdf-compressor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> pdfCompressorTool(@CurrentUser CurrentUserPrincipal user, @Valid @ModelAttribute PdfCompressorRequestDto pdfCompressorRequestDto, HttpServletRequest request, HttpServletResponse response) {
        return pdfCompressorService.compressedPdf(user, pdfCompressorRequestDto);
    }

    @Operation(summary = "Image to PDF", description = "Converts one or more images into a single PDF")
    @PostMapping(value = "/image-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@CurrentUser CurrentUserPrincipal user, @Valid @ModelAttribute ImageToPdfRequestDto dto) {
        return imageToPdfService.convert(user, dto);
    }

    @Operation(summary = "Word to PDF", description = "Converts a .docx file into a PDF")
    @PostMapping(value = "/word-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@CurrentUser CurrentUserPrincipal user, @Valid @ModelAttribute WordToPdfRequestDto dto) {
        return wordToPdfService.convert(user, dto.getToolId(), dto.getSubToolId(), dto.getFile());
    }

    @Operation(summary = "Merge PDFs", description = "Merges multiple PDFs into one, in the order uploaded")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> merge(@CurrentUser CurrentUserPrincipal user,
                                        @Valid @ModelAttribute PdfMergeRequestDto dto) {
        return pdfMergeService.merge(user, dto.getToolId(), dto.getSubToolId(), dto.getFiles());
    }
}
