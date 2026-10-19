package com.devtoolbox.DevToolBoxRevamp.landing.repo;

import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRepository extends JpaRepository<PricingPackage, Integer> {
    List<PricingPackage>findAllByRoleAndIsActiveTrue(String role);
    List<PricingPackage>findAllByIsActiveTrue();
}
