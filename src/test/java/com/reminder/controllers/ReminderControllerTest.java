package com.reminder.controllers;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import com.reminder.config.TestSecurityConfig;
import com.reminder.model.dto.in.ReminderReqDTO;
import com.reminder.model.dto.out.ReminderRespDTO;
import com.reminder.model.entities.Reminder;
import com.reminder.model.entities.User;
import com.reminder.model.repository.ReminderRepo;
import com.reminder.model.repository.UserRepo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReminderRepo reminderRepo;
    @Autowired
    private UserRepo userRepo;

    private User user;
    private Reminder reminder;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp () {
        reminderRepo.deleteAll();
        userRepo.deleteAll();
        user = User.builder().name("Тест Иванович").email("FuckingTests@zaeibali.ru").telegram("@test").build();
        userRepo.save(user);
        reminder = Reminder.builder()
                .title("Интеграционный тест")
                .description("Описание интеграционного теста")
                .deadline(LocalDateTime.now().plusDays(2).withNano(0))
                .user(user).build();
        reminderRepo.save(reminder);
    }


    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder - GET - Тест получения всех напоминаний")
    void whenRequestGetToAllRemindersResponseResultPage(){
        var result = mockMvc.perform(get("/api/v1/reminder").flashAttr("currentUser", user))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        Page<ReminderRespDTO> resultPage = convertToPage(result.getResponse().getContentAsString());

        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getTotalPages()).isEqualTo(1);
        assertThat(resultPage.getNumber()).isZero();
        assertThat(resultPage.getSize()).isEqualTo(10);
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(resultPage.getContent().get(0).getDescription()).isEqualTo(reminder.getDescription());
        assertThat(resultPage.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/... - POST - Тест создания напоминания")
    void whenRequestPostToCreateReminderResponseResultReminder() {
        ReminderReqDTO reminderReqDTO = ReminderReqDTO.builder()
                .title("Новый интеграционный тест")
                .description("Проверка создания нового напоминания")
                .deadline(LocalDateTime.now().plusDays(3))
                .authorId(user.getId())
                .build();
        var result = mockMvc.perform(post("/api/v1/reminder").flashAttr("currentUser", user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reminderReqDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        ReminderRespDTO reminderRespDTO = convertResponseToDTO(result.getResponse().getContentAsString());
        assertThat(reminderRespDTO.getTitle()).isEqualTo(reminderReqDTO.getTitle());
        assertThat(reminderRespDTO.getDescription()).isEqualTo(reminderReqDTO.getDescription());
        assertThat(reminderRespDTO.getDeadline()).isEqualTo(reminderReqDTO.getDeadline());
        assertThat(reminderRespDTO.getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/{reminderId} - GET - Тест получения напоминания по id")
    void whenRequestGetToReminderByIdResponseResultReminder() {
        var result = mockMvc.perform(get("/api/v1/reminder/{id}", reminder.getId()).flashAttr("currentUser", user))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        ReminderRespDTO reminderRespDTO = convertResponseToDTO(result.getResponse().getContentAsString());
        assertThat(reminderRespDTO.getTitle()).isEqualTo(reminder.getTitle());
        assertThat(reminderRespDTO.getDescription()).isEqualTo(reminder.getDescription());
        assertThat(reminderRespDTO.getDeadline()).isEqualTo(reminder.getDeadline().withNano(0));
        assertThat(reminderRespDTO.getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/ - PUT - Тест обновления напоминания")
    void whenRequestPutToUpdateReminderResponseResultReminder() {
        ReminderReqDTO reminderReqDTO = ReminderReqDTO.builder()
                .id(reminder.getId())
                .title("Обновленный интеграционный тест")
                .description("Проверка обновления напоминания")
                .deadline(LocalDateTime.now().plusDays(3))
                .authorId(user.getId())
                .build();
        var result = mockMvc.perform(put("/api/v1/reminder").flashAttr("currentUser", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reminderReqDTO)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        ReminderRespDTO reminderRespDTO = convertResponseToDTO(result.getResponse().getContentAsString());
        assertThat(reminderRespDTO.getTitle()).isEqualTo(reminderReqDTO.getTitle());
        assertThat(reminderRespDTO.getDescription()).isEqualTo(reminderReqDTO.getDescription());
        assertThat(reminderRespDTO.getDeadline()).isEqualTo(reminderReqDTO.getDeadline());
        assertThat(reminderRespDTO.getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/{reminderId} - DELETE - Тест удаления напоминания")
    void whenRequestDeleteToReminderByIdResponseIsEmptyAndReminderIsDeleted() {
        var result = mockMvc.perform(delete("/api/v1/reminder/{id}", reminder.getId()).flashAttr("currentUser", user))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(reminderRepo.findById(reminder.getId())).isNotPresent();
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/search/by-title/{title} - GET - Тест получения напоминания по названию")
    void whenRequestGetToReminderByTitleResponseResultReminderList() {
        var result = mockMvc.perform(get("/api/v1/reminder/search/by-title/{title}", reminder.getTitle()).flashAttr("currentUser", user))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        List<ReminderRespDTO> reminderList = convertToList(result.getResponse().getContentAsString());
        assertThat(reminderList).hasSize(1);
        assertThat(reminderList.get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(reminderList.get(0).getDescription()).isEqualTo(reminder.getDescription());
        assertThat(reminderList.get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(reminderList.get(0).getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/search/by-description/{description} - GET - Тест получения напоминания по описанию")
    void whenRequestGetToReminderByDescriptionResponseResultReminderList() {
        var result = mockMvc.perform(get("/api/v1/reminder/search/by-description/{description}", reminder.getDescription()).flashAttr("currentUser", user))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        List<ReminderRespDTO> reminderList = convertToList(result.getResponse().getContentAsString());
        assertThat(reminderList).hasSize(1);
        assertThat(reminderList.get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(reminderList.get(0).getDescription()).isEqualTo(reminder.getDescription());
        assertThat(reminderList.get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(reminderList.get(0).getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/search/by-deadline/{deadline} - GET - Тест получения напоминания по сроку")
    void whenRequestGetToReminderByDeadlineResponseResultReminderList() {
        var result = mockMvc.perform(get("/api/v1/reminder/search/by-deadline/{deadline}", reminder.getDeadline()).flashAttr("currentUser", user))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
        List<ReminderRespDTO> reminderList = convertToList(result.getResponse().getContentAsString());
        assertThat(reminderList).hasSize(1);
        assertThat(reminderList.get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(reminderList.get(0).getDescription()).isEqualTo(reminder.getDescription());
        assertThat(reminderList.get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(reminderList.get(0).getAuthorId()).isEqualTo(user.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/filter/range - GET - Тест получения напоминания по дате")
    void whenRequestGetToReminderByDateResponseResultReminderPage() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);

        var result = mockMvc.perform(get("/api/v1/reminder/filter/range")
                        .flashAttr("currentUser", user)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "deadline")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).isNotEmpty();

        Page<ReminderRespDTO> page = convertToPage(json);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(page.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
        assertThat(page.getContent().get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(page.getContent().get(0).getDescription()).isEqualTo(reminder.getDescription());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/filter/expired - GET - Тест получения просроченных напоминаний")
    void whenRequestGetToReminderByExpiredResponseResultReminderPage() {
        Reminder expiredReminder = Reminder.builder()
                .title("Просроченное напоминание")
                .description("Это напоминание уже просрочено")
                .deadline(LocalDateTime.now().minusDays(1).withNano(0))
                .user(user)
                .build();
        reminderRepo.save(expiredReminder);
        var result = mockMvc.perform(get("/api/v1/reminder/filter/expired")
                        .flashAttr("currentUser", user)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "deadline")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).isNotEmpty();

        Page<ReminderRespDTO> page = convertToPage(json);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo(expiredReminder.getTitle());
        assertThat(page.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
        assertThat(page.getContent().get(0).getDeadline()).isEqualTo(expiredReminder.getDeadline().withNano(0));
        assertThat(page.getContent().get(0).getDescription()).isEqualTo(expiredReminder.getDescription());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/filter/future - GET - Тест получения напоминаний на будущее")
    void whenRequestGetToReminderByFutureResponseResultReminderPage() {
        var result = mockMvc.perform(get("/api/v1/reminder/filter/future")
                        .flashAttr("currentUser", user)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "deadline")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).isNotEmpty();

        Page<ReminderRespDTO> page = convertToPage(json);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(page.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
        assertThat(page.getContent().get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(page.getContent().get(0).getDeadline()).isAfter(LocalDateTime.now());
        assertThat(page.getContent().get(0).getDescription()).isEqualTo(reminder.getDescription());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/filter/on-date - GET - Тест получения напоминаний на дату")
    void whenRequestGetToReminderBetweenDateResponseResultReminderPage() {
        var result = mockMvc.perform(get("/api/v1/reminder/filter/on-date")
                        .flashAttr("currentUser", user)
                        .param("date", LocalDateTime.now().plusDays(2).toLocalDate().toString())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "deadline")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        assertThat(json).isNotEmpty();
        Page<ReminderRespDTO> page = convertToPage(json);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo(reminder.getTitle());
        assertThat(page.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
        assertThat(page.getContent().get(0).getDeadline()).isEqualTo(reminder.getDeadline());
        assertThat(page.getContent().get(0).getDescription()).isEqualTo(reminder.getDescription());
    }

    @Test
    @SneakyThrows
    @DisplayName("/api/v1/reminder/filter/today - GET - Тест получения напоминаний на сегодня")
    void whenRequestGetToReminderByTodayResponseResultReminderPage() {
        Reminder todayReminder = Reminder.builder()
                .title("Напоминание на сегодня")
                .description("Это напоминание назначено на сегодня")
                .deadline(LocalDateTime.now().withNano(0))
                .user(user)
                .build();
        reminderRepo.save(todayReminder);
        var result = mockMvc.perform(get("/api/v1/reminder/filter/today")
                        .flashAttr("currentUser", user)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "deadline")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        assertThat(json).isNotEmpty();
        Page<ReminderRespDTO> page = convertToPage(json);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo(todayReminder.getTitle());
        assertThat(page.getContent().get(0).getAuthorId()).isEqualTo(user.getId());
        assertThat(page.getContent().get(0).getDeadline()).isEqualTo(todayReminder.getDeadline());
        assertThat(page.getContent().get(0).getDescription()).isEqualTo(todayReminder.getDescription());
    }

    private Page<ReminderRespDTO> convertToPage(String json) {
        List<Map<String, Object>> content = JsonPath.parse(json).read("$.content");
        List<ReminderRespDTO> reminders = content.stream()
                .map(map -> mapper.convertValue(map, ReminderRespDTO.class))
                .collect(Collectors.toList());
        int number = JsonPath.parse(json).read("$.page.number");
        int size = JsonPath.parse(json).read("$.page.size");
        int totalElements = JsonPath.parse(json).read("$.page.totalElements");
        return new PageImpl<>(reminders, PageRequest.of(number, size), totalElements);
    }

    private ReminderRespDTO convertResponseToDTO(String json) {
        return mapper.convertValue(JsonPath.parse(json).read("$"), ReminderRespDTO.class);
    }

    private List<ReminderRespDTO> convertToList(String json) {
        List<Map<String, Object>> content = JsonPath.parse(json).read("$");
        return content.stream()
                .map(map -> mapper.convertValue(map, ReminderRespDTO.class))
                .collect(Collectors.toList());
    }

}