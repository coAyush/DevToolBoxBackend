
package com.devtoolbox.DevToolBoxRevamp.common;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticatedUser {

    private UUID id;
    private String email;
    private String name;
    private String phone;
    private String role;
}
