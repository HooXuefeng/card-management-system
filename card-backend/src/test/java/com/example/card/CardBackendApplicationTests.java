package com.example.card; // 必须和主类包路径一致

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 显式指定主配置类（解决“找不到@SpringBootConfiguration”的核心）
@SpringBootTest(classes = CardBackendApplication.class)
class CardBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}