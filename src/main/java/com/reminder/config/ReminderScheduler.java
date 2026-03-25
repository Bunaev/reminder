package com.reminder.config;

import com.reminder.model.entities.Reminder;
import com.reminder.model.repository.ReminderRepo;
import com.reminder.model.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
public class ReminderScheduler {

    private final ReminderRepo reminderRepo;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkDeadlines() {
        log.info("Проверка предстоящих напоминаний...");
        List<Reminder> reminders = reminderRepo.findPendingNotifications(LocalDateTime.now());
        if (reminders.isEmpty()) {
            log.info("Нет предстоящих напоминаний.");
            return;
        }
        for (Reminder reminder : reminders) {
            try {
                CompletableFuture<Boolean> future = notificationService.sendNotification(reminder);
                future.orTimeout(20, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.error("Ошибка асинхронной отправки: {}", e.getMessage());
                            return null;
                        });
            } catch (Exception e) {
                log.error("Ошибка: {}", e.getMessage());
            }
        }
    }
}
