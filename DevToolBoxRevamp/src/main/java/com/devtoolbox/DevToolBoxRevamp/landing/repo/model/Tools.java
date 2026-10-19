package com.devtoolbox.DevToolBoxRevamp.landing.repo.model;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tools")
@IdClass(ToolsId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tools {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Id
    @Column(name = "sub_id", nullable = false)
    private Integer subId;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private Roles category;

    @Column(name = "tool_category", length = 100, nullable = false)
    private String toolCategory;

    @Column(name = "tool_sub_category", length = 100, nullable = false)
    private String toolSubCategory;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}