package com.reminder.model.repository;


import com.reminder.model.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("testuser")
                .email("test@example.com")
                .telegram("@testuser")
                .password("")
                .build();
        user = userRepo.save(user);
    }

    @Test
    void getUserByName_ShouldReturnUser() {
        User found = userRepo.getUserByName("testuser");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(user.getId());
    }

    @Test
    void getUserByName_WithNonExistingName_ShouldReturnNull() {
        User found = userRepo.getUserByName("nonexisting");
        assertThat(found).isNull();
    }

    @Test
    void getUserByTelegram_ShouldReturnUser() {
        User found = userRepo.getUserByTelegram("@testuser");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(user.getId());
    }

    @Test
    void getUserByTelegram_WithNonExistingTelegram_ShouldReturnNull() {
        User found = userRepo.getUserByTelegram("@nonexisting");
        assertThat(found).isNull();
    }
}
