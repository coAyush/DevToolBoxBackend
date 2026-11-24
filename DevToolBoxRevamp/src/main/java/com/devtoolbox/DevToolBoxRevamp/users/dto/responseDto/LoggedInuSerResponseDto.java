package com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoggedInuSerResponseDto {
    boolean isAuthenticated;
    UUID userId;
    String email;
    String role;
}
