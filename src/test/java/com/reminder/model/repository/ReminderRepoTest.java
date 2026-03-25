package com.reminder.model.repository;

import com.reminder.model.entities.Reminder;
import com.reminder.model.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReminderRepoTest {

    @Autowired
    private ReminderRepo reminderRepo;

    @Autowired
    private UserRepo userRepo;

    private User user;
    private Reminder reminder1;
    private Reminder reminder2;
    private Reminder reminder3;

    @BeforeEach
    void setUp() {
        user = userRepo.save(User.builder()
                .name("testuser")
                .email("test@example.com")
                .telegram("@testuser")
                .password("")
                .build());

        LocalDateTime now = LocalDateTime.now();

        reminder1 = Reminder.builder()
                .title("Тест 1")
                .description("Описание 1")
                .deadline(now.plusDays(1))
                .user(user)
                .build();

        reminder2 = Reminder.builder()
                .title("Тест 2")
                .description("Описание 2")
                .deadline(now.plusDays(2))
                .user(user)
                .build();

        reminder3 = Reminder.builder()
                .title("Другое")
                .description("Другое описание")
                .deadline(now.minusDays(1))
                .user(user)
                .build();

        reminder1 = reminderRepo.save(reminder1);
        reminder2 = reminderRepo.save(reminder2);
        reminder3 = reminderRepo.save(reminder3);
    }

    @Test
    void findByUser_ShouldReturnUserReminders() {
        Page<Reminder> result = reminderRepo.findByUser(user, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void findByTitleContainingIgnoreCaseAndUserId_ShouldReturnMatchingReminders() {
        List<Reminder> result = reminderRepo.findByTitleContainingIgnoreCaseAndUserId("тест", user.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserAndDeadlineBetween_ShouldReturnRemindersInRange() {
        LocalDateTime now = LocalDateTime.now();
        Page<Reminder> result = reminderRepo.findByUserAndDeadlineBetween(
                user, now.minusHours(1), now.plusDays(3), PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void deleteByIdAndUserId_ShouldDeleteReminder() {
        int deleted = reminderRepo.deleteByIdAndUserId(reminder1.getId(), user.getId());
        assertThat(deleted).isEqualTo(1);
    }
}
