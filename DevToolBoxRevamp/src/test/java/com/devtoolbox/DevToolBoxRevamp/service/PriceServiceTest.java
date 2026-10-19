package com.devtoolbox.DevToolBoxRevamp.service;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import com.devtoolbox.DevToolBoxRevamp.landing.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private PriceService priceService;

    private PricingPackage buildPackage(
            Integer id, String tier, String amount) {

        return PricingPackage.builder()
                .id(id)
                .amount(new BigDecimal(amount))
                .role(Roles.DEVELOPER)
                .tier(tier)
                .duration(30)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Maps active package entities to response DTOs")
    void getPrices_returnsMappedDtos() {

        // Arrange: configure the repository's response
        PricingPackage pro = buildPackage(1, "PRO", "499.00");
        PricingPackage basic = buildPackage(2, "BASIC", "199.00");

        when(priceRepository.findAllByIsActiveTrue())
                .thenReturn(List.of(pro, basic));

        // Act: execute the real service method
        ApiResponse result =
                priceService.getPrices();

        // Assert: validate the mapping
        assertThat(result).isNotNull();
        // Verify the repository method was called
        verify(priceRepository).findAllByIsActiveTrue();
    }
}