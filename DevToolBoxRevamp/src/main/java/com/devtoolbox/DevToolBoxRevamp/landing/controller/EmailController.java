package com.devtoolbox.DevToolBoxRevamp.landing.controller;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.landing.dto.requestDto.EmailEnquiryRequestDto;
import com.devtoolbox.DevToolBoxRevamp.landing.service.EnquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {

    private final EnquiryService enquiryService;

    @PostMapping("/submit/enquiry")
    public ResponseEntity<ApiResponse> submitEnquiry(@Valid @RequestBody EmailEnquiryRequestDto enquiryDto){
        return ResponseEntity.ok(enquiryService.submitEnquiry(enquiryDto));
    }
}
