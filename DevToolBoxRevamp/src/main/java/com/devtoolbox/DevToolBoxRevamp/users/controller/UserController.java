package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUser;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name="user dashboard controller",description = "contains endpoint for fetching userdetails")
public class UserController {

    private final UserService userService;

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
}
