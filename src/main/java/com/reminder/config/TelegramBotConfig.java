package com.reminder.config;

import com.reminder.model.service.notification.telegram.ReminderTelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("prod")
public class TelegramBotConfig {

    private final ReminderTelegramBot reminderTelegramBot;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(reminderTelegramBot);
            log.info("✅ Telegram Bot успешно зарегистрирован: {}", reminderTelegramBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("❌ Ошибка регистрации Telegram бота: {}", e.getMessage());
        }
    }
}
