package com.reminder.controllers.Interceptors;

import com.reminder.model.entities.User;
import com.reminder.model.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RequiredArgsConstructor
@RestControllerAdvice
@Slf4j
public class UserInterceptor {
    private final UserService userService;

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return null;
        }
        return userService.saveUserAfterLoginInKeycloak(oidcUser);

    }
}
