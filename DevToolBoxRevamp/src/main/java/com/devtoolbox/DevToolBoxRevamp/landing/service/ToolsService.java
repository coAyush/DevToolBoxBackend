package com.devtoolbox.DevToolBoxRevamp.landing.service;

import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.landing.dto.responseDto.ToolsResponseDto;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.ToolsRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.Tools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ToolsService {
    private final ToolsRepository toolsRepository;
    public ApiResponse getToolsList(){

        log.info("in tools service");
        List<Object[]> toolsList = toolsRepository.getTools();
        log.info(toolsList.toString());
        List<ToolsResponseDto> toolsResponse = toolsList.stream()
                .map(tool -> {
                    log.info("{}", Arrays.toString(tool));
                    return ToolsResponseDto.builder()
                        .id((Integer) tool[0])
                            .category(String.valueOf(tool[1]))
                        .tools_category((String) tool[2])
                        .build();

                })
                .toList();
        return ResponseHelper.commonSuccess("The tools list are :", GetDetails.<List<ToolsResponseDto>>
                builder()
                .details(toolsResponse)
                .build());
    }
}
