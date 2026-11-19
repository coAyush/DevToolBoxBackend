package com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools;

import com.devtoolbox.DevToolBoxRevamp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "url_click_batch_persistence")
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class UrlClickBatchPersistence extends BaseEntity {

    private Long urlId;
    @Column(unique = true, nullable = false)
    private String batchId;
    private Long clicks;
    private Instant lastClickedAt;

}
