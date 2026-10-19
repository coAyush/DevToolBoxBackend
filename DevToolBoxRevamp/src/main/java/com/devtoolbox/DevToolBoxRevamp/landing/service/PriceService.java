package com.devtoolbox.DevToolBoxRevamp.landing.service;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.landing.dto.responseDto.PriceResponseDto;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {
    private final PriceRepository priceRepository;

    public ApiResponse getPrices(){
        log.info("getPrices called");
       List<PriceResponseDto>prices= priceRepository.findAllByIsActiveTrue().stream()
               .map(price->PriceResponseDto.builder()
                       .id(price.getId())
                       .amount(price.getAmount())
                       .role(price.getRole().toString())
                       .tier(price.getTier())
                       .duration(price.getDuration())
                       .build()
               ).toList();
       return ResponseHelper.commonSuccess("Package details are : ", GetDetails.<List<PriceResponseDto>>builder()
                       .details(prices)
               .build());
    }
}
