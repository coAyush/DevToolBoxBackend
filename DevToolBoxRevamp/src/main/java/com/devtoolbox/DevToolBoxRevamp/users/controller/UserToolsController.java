package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUser;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.PdfCompressorRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.QrCodeRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.QrCode;
import com.devtoolbox.DevToolBoxRevamp.users.service.PdfCompressorService;
import com.devtoolbox.DevToolBoxRevamp.users.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users/tools")
@RestController
@RequiredArgsConstructor
@Tag(name = "user tools",description = "this contains all the tools for users")
public class UserToolsController {

    private final QrCodeService qrCodeService;
    private final PdfCompressorService pdfCompressorService;

    @Operation(description = "qr code generator",summary = "genrates the qr code of any url with this endpoint")
    @PostMapping("/qr-code")
    public ResponseEntity<ApiResponse> qrCodeGenerator(@CurrentUser CurrentUserPrincipal user, @Valid @RequestBody QrCodeRequestDto qrCodeDto){
        return ResponseEntity.ok(qrCodeService.generateQr(user,qrCodeDto));
    }

    @Operation(description = "pdf compressor",summary = "compresses the pdf code with levels specified")
    @PostMapping(value = "/pdf-compressor",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> pdfCompressorTool(@CurrentUser CurrentUserPrincipal user, @Valid @ModelAttribute PdfCompressorRequestDto pdfCompressorRequestDto, HttpServletRequest request, HttpServletResponse response){
        return pdfCompressorService.compressedPdf(user,pdfCompressorRequestDto);
    }


}
