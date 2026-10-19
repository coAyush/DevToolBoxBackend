package com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerificationDto {

    @Email
    @NotNull
    private String email;

    @NotBlank
    @Size(max = 6)
    private String otp;
}
