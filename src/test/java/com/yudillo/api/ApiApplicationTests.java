package com.yudillo.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** 스프링 컨텍스트가 정상적으로 뜨는지 확인한다. */
@ActiveProfiles("test")
@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
