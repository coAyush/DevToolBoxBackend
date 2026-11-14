package com.devtoolbox.DevToolBoxRevamp.health;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/health")
@RestController
public class HeathController {

    @GetMapping("/active")
    public ResponseEntity<ApiResponse>keepServerActive(){
        return ResponseEntity.ok(ResponseHelper.success("Server is active"));
    }
}
