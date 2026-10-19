package com.devtoolbox.DevToolBoxRevamp.landing.repo.model;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="package")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PricingPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(nullable = false, length = 50)
    private String tier;

    @Column(nullable = false)
    private Integer duration;

    @Column
    @Builder.Default
    private Boolean isActive=true;
}
