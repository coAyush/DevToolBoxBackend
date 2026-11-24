package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUser;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.PaymentFailedRequest;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.VerifyPaymentRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.service.PaymentService;
import com.devtoolbox.DevToolBoxRevamp.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name="user dashboard controller",description = "contains endpoint for fetching userdetails")
public class UserController {

    private final UserService userService;
    private final PaymentService paymentService;

    @PostMapping("/me")
    @Operation(description = "checking user logged in",summary ="check for the user being authenticated or not")
    public ResponseEntity<ApiResponse>getLoggedInUser(@CookieValue(value = "accessToken",required = false)String accessToken, @CookieValue(value = "refreshToken",required = false)String refreshToken, HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok(userService.checkLoggenIn(request,response,accessToken,refreshToken));
    }
    @GetMapping("/dashboard")
    @Operation(description = "dashboard endpoint",summary = "gives users details")
    public ResponseEntity<ApiResponse> getUserDetails(@CurrentUser CurrentUserPrincipal currentUserPrincipal) {
        log.info(currentUserPrincipal.email);
        return ResponseEntity.ok(userService.dashboardDetails(currentUserPrincipal));
    }

    @GetMapping("/tools-suite")
    @Operation(description = "dashboard endpoint",summary = "gives users details")
    public ResponseEntity<ApiResponse> getToolsSuite(@CurrentUser CurrentUserPrincipal currentUserPrincipal) {
        log.info(currentUserPrincipal.email);
        return ResponseEntity.ok(userService.toolSuite(currentUserPrincipal));
    }

    @GetMapping("/sub-tools-suite")
    @Operation(description = "dashboard endpoint",summary = "gives users details")
    public ResponseEntity<ApiResponse> getSubToolsSuite(@CurrentUser CurrentUserPrincipal currentUserPrincipal, @RequestParam Integer toolId) {
        log.info(currentUserPrincipal.email);
        return ResponseEntity.ok(userService.subtoolSuite(currentUserPrincipal,toolId));
    }

    @GetMapping("/plans")
    @Operation(description = "user plans endpoint",summary = "gives users plans pricing details")
    public ResponseEntity<ApiResponse> getUsersPlanDetail(@CurrentUser CurrentUserPrincipal user) {
        return ResponseEntity.ok(userService.getPlansDetails(user));
    }

    @GetMapping("/profile-details")
    @Operation(description = "user profile endpoint",summary = "gives users profile details")
    public ResponseEntity<ApiResponse> getProfileDetails(@CurrentUser CurrentUserPrincipal user) {
        return ResponseEntity.ok(userService.getProfileData(user));
    }


    @Operation(summary = "Payment request endpoint",description = "endpoint used to initaie a payment request")
    @PostMapping("/payment-create")
    public ResponseEntity<ApiResponse>createPayment(@CurrentUser CurrentUserPrincipal user,@RequestParam("packageId")Integer packageId){
        return ResponseEntity.ok(paymentService.createPaymentRequest(user,packageId));
    }
    @PostMapping("/payment/verify")
    public ResponseEntity<ApiResponse> verify(@CurrentUser CurrentUserPrincipal user, @Valid @RequestBody VerifyPaymentRequestDto req) {
        return ResponseEntity.ok(paymentService.verifyAndActivate(user, req));
    }

    @PostMapping("/payment/failed")
    public ResponseEntity<ApiResponse> failed(@CurrentUser CurrentUserPrincipal user,
                                              @Valid @RequestBody PaymentFailedRequest req) {
        return ResponseEntity.ok(paymentService.recordFailure(user, req.orderId(), req.reason()));
    }
}
