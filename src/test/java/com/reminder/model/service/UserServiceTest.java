package com.reminder.model.service;

import com.reminder.model.entities.User;
import com.reminder.model.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private OidcUser oidcUser;
    private User existingUser;
    private User newUser;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TELEGRAM = "@testuser";
    private static final Long TEST_CHAT_ID = 123456789L;

    @BeforeEach
    void setUp() {
        oidcUser = mock(OidcUser.class);

        existingUser = User.builder()
                .id(1L)
                .name(TEST_USERNAME)
                .email(TEST_EMAIL)
                .telegram(TEST_TELEGRAM)
                .build();

        newUser = User.builder()
                .name(TEST_USERNAME)
                .email(TEST_EMAIL)
                .telegram(TEST_TELEGRAM)
                .build();
    }

    @Test
    void whenSaveUserAfterLoginInKeycloakWithExistingUserThenReturnExistingUser() {
        when(oidcUser.getPreferredUsername()).thenReturn(TEST_USERNAME);
        when(oidcUser.getEmail()).thenReturn(TEST_EMAIL);
        when(oidcUser.getAttribute("telegram")).thenReturn(TEST_TELEGRAM);
        when(userRepo.getUserByName(TEST_USERNAME)).thenReturn(existingUser);

        User result = userService.saveUserAfterLoginInKeycloak(oidcUser);

        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(TEST_USERNAME, result.getName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_TELEGRAM, result.getTelegram());
        verify(userRepo).getUserByName(TEST_USERNAME);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void whenSaveUserAfterLoginInKeycloakWithNewUserThenCreateAndReturnNewUser() {
        when(oidcUser.getPreferredUsername()).thenReturn(TEST_USERNAME);
        when(oidcUser.getEmail()).thenReturn(TEST_EMAIL);
        when(oidcUser.getAttribute("telegram")).thenReturn(TEST_TELEGRAM);
        when(userRepo.getUserByName(TEST_USERNAME)).thenReturn(null);
        when(userRepo.save(any(User.class))).thenReturn(newUser);

        User result = userService.saveUserAfterLoginInKeycloak(oidcUser);

        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_TELEGRAM, result.getTelegram());
        verify(userRepo).getUserByName(TEST_USERNAME);
        verify(userRepo).save(any(User.class));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User savedUser = captor.getValue();
        assertNull(savedUser.getId());
        assertEquals(TEST_USERNAME, savedUser.getName());
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertEquals(TEST_TELEGRAM, savedUser.getTelegram());
    }

    @Test
    void whenSaveUserAfterLoginInKeycloakWithNullTelegramThenCreateUserWithNullTelegram() {
        when(oidcUser.getPreferredUsername()).thenReturn(TEST_USERNAME);
        when(oidcUser.getEmail()).thenReturn(TEST_EMAIL);
        when(oidcUser.getAttribute("telegram")).thenReturn(null);
        when(userRepo.getUserByName(TEST_USERNAME)).thenReturn(null);

        User userWithoutTelegram = User.builder()
                .name(TEST_USERNAME)
                .email(TEST_EMAIL)
                .telegram(null)
                .build();
        when(userRepo.save(any(User.class))).thenReturn(userWithoutTelegram);

        User result = userService.saveUserAfterLoginInKeycloak(oidcUser);

        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertNull(result.getTelegram());
        verify(userRepo).getUserByName(TEST_USERNAME);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void whenUpdateTelegramChatIdWithExistingUserThenUpdateChatId() {
        when(userRepo.getUserByTelegram("@" + TEST_USERNAME)).thenReturn(existingUser);

        userService.updateTelegramChatId(TEST_USERNAME, TEST_CHAT_ID);

        verify(userRepo).getUserByTelegram("@" + TEST_USERNAME);
        assertEquals(TEST_CHAT_ID, existingUser.getTelegramChatId());
    }

    @Test
    void whenUpdateTelegramChatIdWithNonExistingUserThenThrowException() {
        when(userRepo.getUserByTelegram("@" + TEST_USERNAME)).thenReturn(null);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.updateTelegramChatId(TEST_USERNAME, TEST_CHAT_ID));
        assertEquals("Cannot invoke \"com.reminder.model.entities.User.setTelegramChatId(java.lang.Long)\" because \"user\" is null",
                exception.getMessage());
        verify(userRepo).getUserByTelegram("@" + TEST_USERNAME);
    }

    @Test
    void whenUpdateTelegramChatIdWithNullChatIdThenUpdateWithNull() {
        when(userRepo.getUserByTelegram("@" + TEST_USERNAME)).thenReturn(existingUser);
        existingUser.setTelegramChatId(TEST_CHAT_ID);

        userService.updateTelegramChatId(TEST_USERNAME, null);

        verify(userRepo).getUserByTelegram("@" + TEST_USERNAME);
        assertNull(existingUser.getTelegramChatId());
    }
}
