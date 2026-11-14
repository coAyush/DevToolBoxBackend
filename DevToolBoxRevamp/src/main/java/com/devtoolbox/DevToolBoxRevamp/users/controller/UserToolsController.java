package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUser;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.QrCodeRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.QrCode;
import com.devtoolbox.DevToolBoxRevamp.users.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users/tools")
@RestController
@RequiredArgsConstructor
@Tag(name = "user tools",description = "this contains all the tools for users")
public class UserToolsController {

    private final QrCodeService qrCodeService;

    @Operation(description = "qr code generator",summary = "genrates the qr code of any url with this endpoint")
    @PostMapping("/qr-code")
    public ResponseEntity<ApiResponse> qrCodeGenerator(@CurrentUser CurrentUserPrincipal user, @RequestBody QrCodeRequestDto qrCodeDto){
        return ResponseEntity.ok(qrCodeService.generateQr(user,qrCodeDto));
    }
}
