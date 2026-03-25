package com.reminder.model.service.notification.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@Profile("prod")
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(@Value("${spring.mail.host}") String host,
                       @Value("${spring.mail.port}") int port,
                       @Value("${spring.mail.username}") String username,
                       @Value("${spring.mail.password}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.debug", "true");

        this.mailSender = sender;
        log.info("✅ MailService готов!");

    }


    public boolean sendEmail(String to, String subject, String text) {
        log.info("Отправка email на {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("remindernotification@internet.ru");

            mailSender.send(message);
            log.info("✅ Email отправлен на {}", to);
            return true;

        } catch (Exception e) {
            log.error("❌ Ошибка email: {}", e.getMessage());
            return false;
        }
    }
}