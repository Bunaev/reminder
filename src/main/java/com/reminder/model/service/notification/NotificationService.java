package com.reminder.model.service.notification;

import com.reminder.model.entities.Reminder;
import com.reminder.model.repository.ReminderRepo;
import com.reminder.model.service.notification.email.MailService;
import com.reminder.model.service.notification.telegram.ReminderTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("prod")
public class NotificationService {

    private final ReminderRepo reminderRepo;
    private final MailService emailService;
    private final ReminderTelegramBot bot;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int MAX_TRY_COUNT = 5;
    @Async
    @Transactional
    public CompletableFuture<Boolean> sendNotification(Reminder reminder) {
        Reminder managedReminder = reminderRepo.findById(reminder.getId())
                .orElseThrow(() -> new RuntimeException("Напоминание не найдено."));
        String message = String.format(
                "🔔 *НАПОМИНАНИЕ!* 🔔\n\n" +
                        "━━━━━━━━━━━━━━━━━━━\n" +
                        "📌 *%s*\n" +
                        "📝 %s\n" +
                        "━━━━━━━━━━━━━━━━━━━\n" +
                        "⏰ %s\n" +
                        "━━━━━━━━━━━━━━━━━━━\n\n" +
                        "⚡️ Не пропустите важное событие!\n" +
                        " ❤ С Уважением, твой Reminder!",
                managedReminder.getTitle(),
                managedReminder.getDescription(),
                managedReminder.getDeadline().format(formatter)
        );

        boolean emailSend = trySendEmail(managedReminder, message);
        boolean telegramSend = trySendTelegram(managedReminder, message);

        return CompletableFuture.completedFuture(emailSend || telegramSend);
    }

    private boolean trySendEmail(Reminder reminder, String message) {
        String email = reminder.getUser().getEmail();
        if (!reminder.isEmailSend() && email != null) {
            for (int i = 0; i < NotificationService.MAX_TRY_COUNT; i++) {
                log.info("Попытка отправки E-mail №{} на электронную почту: {}", i + 1, reminder.getUser().getEmail());
                if (emailService.sendEmail(email, "Напоминание", message)) {
                    reminder.setEmailSend(true);
                    return true;
                }
            }
        }
        reminder.setEmailSend(true);
        return false;
    }
    private boolean trySendTelegram(Reminder reminder, String message) {
        Long telegramChatId = reminder.getUser().getTelegramChatId();
        if (!reminder.isTelegramSend() && telegramChatId != null) {
            for (int i = 0; i < NotificationService.MAX_TRY_COUNT; i++) {
                log.info("Попытка отправки сообщения №{} в Телеграм пользователю: {}", i + 1, reminder.getUser().getTelegram());
                if (bot.sendText(telegramChatId, message)) {
                    reminder.setTelegramSend(true);
                    return true;
                }
            }
        } if (telegramChatId == null) {
            log.info("Пользователь {} не зарегистрирован в Telegram или не написал боту: {}", reminder.getUser().getName(), bot.getBotUsername());
        }
        reminder.setTelegramSend(true);
        return false;
    }
}
