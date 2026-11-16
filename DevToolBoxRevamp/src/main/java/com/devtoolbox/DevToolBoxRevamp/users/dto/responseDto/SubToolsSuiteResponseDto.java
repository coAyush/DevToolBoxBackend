package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class SubToolsSuiteResponseDto {
        private Integer subToolId;
        private String subToolCategory;
    }


