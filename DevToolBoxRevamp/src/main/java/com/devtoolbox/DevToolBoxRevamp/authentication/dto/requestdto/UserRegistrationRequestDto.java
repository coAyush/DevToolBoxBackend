package com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AccountType;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequestDto {
    @NotNull
    @Size(min = 1, max = 100,message = "length must be between 1 to 100")
    private String name;
    @NotNull
    @Email
    private String email;
    @NotNull
    @Size(min = 1, max = 15,message = "length must be between 1 to 10")
    private String phone;
    @NotNull
    private String password;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Builder.Default
    private Boolean isVerified = false;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private AccountType accountType=AccountType.LOCAL;
    private String imageUrl;
}
