package com.reminder.model.service.notification.telegram;

import com.reminder.model.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@RequiredArgsConstructor
@Slf4j
@Getter
@Profile("prod")
public class ReminderTelegramBot extends TelegramLongPollingBot {
    private final UserService userService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            if (text.equals("/start")) {
                userService.updateTelegramChatId(username, chatId);
                sendText(chatId, "👋 *Привет, @" + username + "!*\n\n" +
                        "🎉 *Добро пожаловать в Reminder Bot!*\n" +
                        "—————————————\n\n" +
                        "🔔 Я буду напоминать тебе о важных делах:\n" +
                        "• 📝 Встречи и задачи\n" +
                        "• 🎂 Дни рождения\n" +
                        "• 💼 Рабочие дедлайны\n" +
                        "• 🏋️‍♂️ Тренировки");
            }
        }
    }

    public boolean sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
            log.info("✅ Сообщение отправлено в чат: {}, текст: {}", chatId, text);
            return true;
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
            return false;
        }
    }
}
