package com.devtoolbox.DevToolBoxRevamp.landing.repo;

import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.Tools;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.ToolsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToolsRepository extends JpaRepository<Tools, ToolsId> {

    @Query(value = """
            SELECT DISTINCT id, category, tool_category
            FROM tools
            WHERE is_active = TRUE
            ORDER BY id
            """, nativeQuery = true)
    List<Object []> getTools();
}
