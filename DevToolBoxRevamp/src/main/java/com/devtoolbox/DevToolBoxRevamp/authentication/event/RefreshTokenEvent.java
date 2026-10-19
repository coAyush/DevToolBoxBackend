package com.devtoolbox.DevToolBoxRevamp.authentication.event;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEvent {
    private User user;
    private String refreshToken;
}
