package com.devtoolbox.DevToolBoxRevamp.landing.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.Tools;
import com.devtoolbox.DevToolBoxRevamp.landing.service.ToolsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Tools Controller",description = "contains api related for tools for landing page")
@RequiredArgsConstructor
public class ToolsController {
    private final ToolsService toolsService;

    @GetMapping("/tools/list")
    @Operation(description = "this contains tools for getting the category for tools ",summary = "endpoint for fetching tools for both developer and user category")
    public ResponseEntity<ApiResponse> findTools(){
        return ResponseEntity.ok(toolsService.getToolsList());
    }

}
