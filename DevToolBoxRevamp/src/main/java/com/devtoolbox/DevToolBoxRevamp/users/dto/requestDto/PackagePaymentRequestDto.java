package com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackagePaymentRequestDto {

    private Integer packageId;
}
