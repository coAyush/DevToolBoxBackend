package com.devtoolbox.DevToolBoxRevamp.users.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name="user dashboard controller",description = "contains endpoint for fetching userdetails")
public class UserDashboardController {

    @GetMapping("/dashboard")
    @Operation(description = "dashboard endpoint",summary = "givess users details")
    public ResponseEntity<String> getUserDetails() {
        return ResponseEntity.ok("Dashboard");
    }
}
