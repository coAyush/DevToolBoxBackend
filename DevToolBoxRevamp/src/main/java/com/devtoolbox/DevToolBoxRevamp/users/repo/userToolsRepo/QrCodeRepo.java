package com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepo  extends JpaRepository<QrCode, String> {

    boolean existsByUrl(String url);
    QrCode findByUrl(String url);
    boolean existsByCloudinaryUrl(String cloudinaryUrl);
}
