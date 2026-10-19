package com.devtoolbox.DevToolBoxRevamp.authentication.controller;

import com.devtoolbox.DevToolBoxRevamp.authentication.service.AuthService;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.LoginRequestDto;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.OtpVerificationDto;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.UserRegistrationRequestDto;
import com.devtoolbox.DevToolBoxRevamp.exception.GenericException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Auth Controller",description = "this is used to do all registration login purpose")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(description = "register user",summary = "endpoint used to register normal user")
    public ResponseEntity<ApiResponse>registerNormalUser(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto) throws GenericException {
        return ResponseEntity.ok(authService.registerUser(userRegistrationRequestDto));
    }

    @PostMapping("/resendOtp")
    @Operation(description = "otp resent",summary = "endpoint used to resend otp when requested or endpoint being hit")
    public ResponseEntity<ApiResponse>resendOtp(@RequestParam("email") String email) throws GenericException {
        return ResponseEntity.ok(authService.resendOtp(email));
    }

    @PostMapping("/verifyOtp")
    @Operation(description = "register user",summary = "endpoint used to register  user")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerificationDto otpVerificationDto,HttpServletResponse response,HttpServletRequest request) throws GenericException {
       return ResponseEntity.ok(authService.verifyOtp(otpVerificationDto,request,response));
    }
    @PostMapping("/login")
    @Operation(description = "login user",summary = "endpoint used to login  user")
    public ResponseEntity<ApiResponse>loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto){
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @GetMapping("/oauth/login")
    @Operation(description = "oauth authentication",summary = "endpoint used for oauth authentication")
    public void oauthAuthentication(@RequestParam("role")String role, @RequestParam("provider")String provider, HttpServletRequest request, HttpServletResponse response) throws GenericException, IOException {
            authService.oatuhLogin(role,provider,request,response);
        }
}
