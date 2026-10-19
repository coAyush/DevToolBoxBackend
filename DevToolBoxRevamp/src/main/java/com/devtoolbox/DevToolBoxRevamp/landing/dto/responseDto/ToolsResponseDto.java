package com.devtoolbox.DevToolBoxRevamp.landing.dto.responseDto;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolsResponseDto {
    private Integer id;
    private String category;
    private String tools_category;
}
