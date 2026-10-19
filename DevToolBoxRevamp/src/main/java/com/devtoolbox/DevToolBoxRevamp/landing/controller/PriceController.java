package com.devtoolbox.DevToolBoxRevamp.landing.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.landing.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Pricing", description = "APIs for pricing plans")
public class PriceController {
    private  final PriceService priceservice;

    @Operation(description = "get prices",summary = "get all prices filtered by role")
    @GetMapping("/prices")
    public ResponseEntity<ApiResponse> findAllByRole(){
        return ResponseEntity.ok(priceservice.getPrices());
    }
}