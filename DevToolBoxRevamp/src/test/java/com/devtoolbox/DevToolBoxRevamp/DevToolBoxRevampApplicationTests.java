package com.devtoolbox.DevToolBoxRevamp;

import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@SpringBootTest
@EnableScheduling

class DevToolBoxRevampApplicationTests {
	@Autowired
	private  ToolUsageRepository toolUsageRepository;
	@Test
	void contextLoads() {
	}



}
