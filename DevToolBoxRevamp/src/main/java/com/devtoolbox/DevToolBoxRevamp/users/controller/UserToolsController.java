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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

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
    private final UrlService urlService;

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

    @Operation(summary = "URL controller", description = "used to get the shortenend url")
    @PostMapping("/shorten-url")
    public ResponseEntity<ApiResponse>getShortUrl(@CurrentUser CurrentUserPrincipal user,@Valid @RequestBody UrlRequestDto urlRequest){
        return ResponseEntity.ok(urlService.createUrl(user,urlRequest.getLongUrl(), urlRequest.getAlias(), urlRequest.getToolId(), urlRequest.getSubToolId()));
    }

    @Operation(summary = "URL dashboard controller", description = "used to get the url dashboard details")
    @PostMapping("/url")
    public ResponseEntity<ApiResponse>getUrlDashboardAnalytics(@CurrentUser CurrentUserPrincipal user,@Valid @RequestBody UrlDashboardRequestDto urlDashboardDto){
        return ResponseEntity.ok(urlService.getUrlDashboardAnalytics(user,urlDashboardDto.getToolId(), urlDashboardDto.getSubToolId()));
    }

}
