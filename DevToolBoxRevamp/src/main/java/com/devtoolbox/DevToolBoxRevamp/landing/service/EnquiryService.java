package com.devtoolbox.DevToolBoxRevamp.landing.service;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.EmailService;
import com.devtoolbox.DevToolBoxRevamp.landing.dto.requestDto.EmailEnquiryRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnquiryService {
    private final EmailService emailService;

    public ApiResponse submitEnquiry(EmailEnquiryRequestDto enquiryDto){
        log.info("submitEnquiry");
        String to="librarysystemxwz@gmail.com";
        String enquiryTopic= enquiryDto.getInquiryTopic();
        String name=enquiryDto.getName();
        String message=enquiryDto.getMessage();
        String content="Enquiry topic : "+enquiryTopic+"\n"+"Name: "+name+"\n"+"Message: "+message;
        emailService.sendMessage(to, enquiryDto.subject,content);
        return ResponseHelper.success("Enquiry sent successfully");
    }
}
