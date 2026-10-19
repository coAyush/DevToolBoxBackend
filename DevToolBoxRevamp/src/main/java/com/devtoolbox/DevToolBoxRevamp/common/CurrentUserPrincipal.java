package com.devtoolbox.DevToolBoxRevamp.common;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserPrincipal {
    public UUID userId;
    public String name;
    public String email;
    public Roles role;
    public String phone;
}
