package com.devtoolbox.DevToolBoxRevamp.landing.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailEnquiryRequestDto {
    @NotBlank
    public String subject;
    @NotBlank
    public String name;
    @NotBlank
    @Email
    public String email;
    @NotBlank
    public String inquiryTopic;
    @NotBlank
    @Size(min=5, max=250)
    public String message;
}
