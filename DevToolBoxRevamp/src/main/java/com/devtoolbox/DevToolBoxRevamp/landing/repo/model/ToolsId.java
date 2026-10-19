package com.devtoolbox.DevToolBoxRevamp.landing.repo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ToolsId implements Serializable {

    private Integer id;
    private Integer subId;
}