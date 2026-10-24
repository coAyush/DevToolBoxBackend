package com.devtoolbox.DevToolBoxRevamp.users.repo.model;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "tools_usage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ToolUsage extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tool_id", nullable = false)
    private Integer toolId;

    @Column(name = "tool_sub_id", nullable = false)
    private Integer toolSubId;
}