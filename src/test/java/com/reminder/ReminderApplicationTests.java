package com.reminder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DisplayName("Smoke tests for ReminderApplication")
@ActiveProfiles("test")
class ReminderApplicationTests {

    @Test
    void contextLoads() {
    }

}
