package com.reminder.model.service;

import com.reminder.model.entities.User;
import com.reminder.model.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepo userRepo;
    @Transactional
    public User saveUserAfterLoginInKeycloak(OidcUser oidcUser) {
        String username = oidcUser.getPreferredUsername();
        String email = oidcUser.getEmail();
        String telegram = oidcUser.getAttribute("telegram");
        User user = userRepo.getUserByName(username);
        if (user == null) {
            user = User.builder()
                    .name(username)
                    .email(email)
                    .telegram(telegram)
                    .build();
            userRepo.save(user);
        }
        return user;
        }
    @Transactional
    public void updateTelegramChatId(String username, Long telegramChatId) {
        User user = userRepo.getUserByTelegram("@" + username);
        user.setTelegramChatId(telegramChatId);
    }
}
