package com.devtoolbox.DevToolBoxRevamp.repo;

import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PriceRepositoryTest {

    @Autowired
    private PriceRepository priceRepository;

    // Helper method
    private PricingPackage savePackage(
            Roles role, String tier, Boolean isActive) {

        return priceRepository.save(
                PricingPackage.builder()
                        .amount(new BigDecimal("199.50"))
                        .role(role)
                        .duration(30)
                        .isActive(isActive)
                        .tier(tier)
                        .build()
        );
    }

    @Test
    @DisplayName("Return only active packages")
    void findAllByIsActiveTrue_returnsOnlyActivePackages() {

        // Arrange
        savePackage(Roles.DEVELOPER, "PRO", true);
        savePackage(Roles.USER, "BASIC", true);
        savePackage(Roles.USER, "INACTIVE", false);

        // Act
        List<PricingPackage> packages =
                priceRepository.findAllByIsActiveTrue();

        // Assert
        assertThat(packages)
                .hasSizeGreaterThan(1)
                .allSatisfy(pkg ->
                        assertThat(pkg.getIsActive()).isTrue()
                );
    }
}