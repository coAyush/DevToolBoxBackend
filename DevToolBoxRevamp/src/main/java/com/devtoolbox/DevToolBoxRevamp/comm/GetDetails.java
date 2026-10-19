package com.devtoolbox.DevToolBoxRevamp.comm;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDetails <T> extends Metadata{
  T details;
}
