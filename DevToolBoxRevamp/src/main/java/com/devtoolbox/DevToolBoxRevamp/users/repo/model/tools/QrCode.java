package com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "qrcode")
public class QrCode extends BaseEntity {
    private String url;
    private String cloudinaryUrl;
}
