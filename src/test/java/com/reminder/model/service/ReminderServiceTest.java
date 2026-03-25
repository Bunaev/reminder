package com.reminder.model.service;

import com.reminder.model.dto.ReminderMapper;
import com.reminder.model.dto.in.ReminderReqDTO;
import com.reminder.model.dto.out.ReminderRespDTO;
import com.reminder.model.entities.Reminder;
import com.reminder.model.entities.User;
import com.reminder.model.repository.ReminderRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ReminderServiceTest {

    @Mock
    private ReminderRepo reminderRepo;

    @Mock
    private ReminderMapper reminderMapper;

    @InjectMocks
    private ReminderService reminderService;

    private User user;
    private Reminder reminder;
    private ReminderRespDTO reminderRespDTO;
    private ReminderReqDTO reminderReqDTO;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        reminder = Reminder.builder().id(1L).user(user).build();
        reminderRespDTO = ReminderRespDTO.builder().id(1L).build();
        reminderReqDTO = ReminderReqDTO.builder()
                .id(1L)
                .title("Тестовый заголовок")
                .description("Тестовое описание")
                .deadline(LocalDateTime.now().plusDays(1))
                .authorId(user.getId())
                .build();
    }

    @Test
    void whenCreateReminderForUserThenReturnReminderRespDTO() {
        when(reminderRepo.save(any(Reminder.class))).thenReturn(reminder);
        when(reminderMapper.toEntity(reminderReqDTO)).thenReturn(reminder);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);
        Optional<ReminderRespDTO> result = reminderService.createReminderForUser(user, reminderReqDTO);
        assertTrue(result.isPresent());
        assertEquals(reminderRespDTO, result.get());
        verify(reminderRepo).save(reminder);
        verify(reminderMapper).toEntity(reminderReqDTO);
        verify(reminderMapper).toDto(reminder);
    }

    @Test
    void whenGetReminderForUserThenReturnReminderDTO() {
        when(reminderRepo.findById(anyLong())).thenReturn(Optional.of(reminder));
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);
        Optional<ReminderRespDTO> result = reminderService.getUserReminder(user, reminderReqDTO.getId());
        assertTrue(result.isPresent());
        assertEquals(reminderRespDTO, result.get());
        verify(reminderRepo).findById(reminder.getId());
        verify(reminderMapper).toDto(reminder);
    }

    @Test
    void whenUpdateReminderForUserReturnUpdatedReminderDTO() {
        ReminderReqDTO reminderUpdateReqDTO = ReminderReqDTO.builder()
                .id(1L)
                .title("Измененный тестовый заголовок.")
                .description("Измененное тестовое описание.")
                .deadline(LocalDateTime.now().plusDays(2))
                .authorId(user.getId()).build();

        ReminderRespDTO updatedRespDTO = ReminderRespDTO.builder()
                .id(1L)
                .title(reminderUpdateReqDTO.getTitle())
                .description(reminderUpdateReqDTO.getDescription())
                .deadline(reminderUpdateReqDTO.getDeadline())
                .build();

        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(updatedRespDTO);
        when(reminderRepo.findById(anyLong())).thenReturn(Optional.of(reminder));
        when(reminderRepo.save(any(Reminder.class))).thenReturn(reminder);

        Optional<ReminderRespDTO> result = reminderService.updateUserReminder(user, reminderUpdateReqDTO);
        assertTrue(result.isPresent());
        assertEquals(reminderUpdateReqDTO.getTitle(), result.get().getTitle());
        assertEquals(reminderUpdateReqDTO.getDescription(), result.get().getDescription());

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepo).save(captor.capture());

        Reminder savedReminder = captor.getValue();
        assertEquals(reminderUpdateReqDTO.getTitle(), savedReminder.getTitle());
        assertEquals(reminderUpdateReqDTO.getDescription(), savedReminder.getDescription());
        assertEquals(reminderUpdateReqDTO.getDeadline(), savedReminder.getDeadline());

        verify(reminderMapper).toDto(any(Reminder.class));
    }

    @Test
    void whenGetAllUserRemindersThenReturnPageOfReminderRespDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));
        when(reminderRepo.findByUser(user, pageable)).thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);
        Page<ReminderRespDTO> result = reminderService.getAllUserReminders(user, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reminderRespDTO, result.getContent().get(0));
        verify(reminderRepo).findByUser(user, pageable);
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenDeleteReminderExistsAndBelongsToUserThenSuccess() {
        Long reminderId = 1L;
        Long userId = 1L;

        when(reminderRepo.deleteByIdAndUserId(reminderId, userId)).thenReturn(1);
        assertDoesNotThrow(() -> reminderService.deleteReminder(user, reminderId));
        verify(reminderRepo).deleteByIdAndUserId(reminderId, userId);
    }

    @Test
    void whenDeleteReminderDoesNotExistOrNotBelongToUserThenThrowException() {
        Long reminderId = 1L;
        Long userId = 1L;

        when(reminderRepo.deleteByIdAndUserId(reminderId, userId)).thenReturn(0);
        assertThrows(EntityNotFoundException.class,
                () -> reminderService.deleteReminder(user, reminderId));
        verify(reminderRepo).deleteByIdAndUserId(reminderId, userId);
    }

    @Test
    void whenGetReminderByTitleWithExistingTitleThenReturnListOfReminderRespDTO() {
        String searchTitle = "Тест";
        List<Reminder> reminders = List.of(reminder);

        when(reminderRepo.findByTitleContainingIgnoreCaseAndUserId(searchTitle, user.getId()))
                .thenReturn(reminders);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        List<ReminderRespDTO> result = reminderService.getReminderByTitle(user, searchTitle);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reminderRespDTO, result.get(0));
        verify(reminderRepo).findByTitleContainingIgnoreCaseAndUserId(searchTitle, user.getId());
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetReminderByTitleWithNonExistingTitleThenReturnEmptyList() {
        String searchTitle = "Несуществующий";

        when(reminderRepo.findByTitleContainingIgnoreCaseAndUserId(searchTitle, user.getId()))
                .thenReturn(Collections.emptyList());

        List<ReminderRespDTO> result = reminderService.getReminderByTitle(user, searchTitle);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reminderRepo).findByTitleContainingIgnoreCaseAndUserId(searchTitle, user.getId());
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetReminderByDescriptionWithExistingDescriptionThenReturnListOfReminderRespDTO() {
        String searchDescription = "Тестовое";
        List<Reminder> reminders = List.of(reminder);

        when(reminderRepo.findByDescriptionContainingIgnoreCaseAndUserId(searchDescription, user.getId()))
                .thenReturn(reminders);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        List<ReminderRespDTO> result = reminderService.getReminderByDescription(user, searchDescription);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reminderRespDTO, result.get(0));
        verify(reminderRepo).findByDescriptionContainingIgnoreCaseAndUserId(searchDescription, user.getId());
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetReminderByDescriptionWithNonExistingDescriptionThenReturnEmptyList() {
        String searchDescription = "Несуществующее";

        when(reminderRepo.findByDescriptionContainingIgnoreCaseAndUserId(searchDescription, user.getId()))
                .thenReturn(Collections.emptyList());

        List<ReminderRespDTO> result = reminderService.getReminderByDescription(user, searchDescription);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reminderRepo).findByDescriptionContainingIgnoreCaseAndUserId(searchDescription, user.getId());
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetReminderByDeadlineWithExistingDateThenReturnListOfReminderRespDTO() {
        LocalDateTime searchDeadline = LocalDateTime.now();
        LocalDateTime startOfDay = searchDeadline.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = searchDeadline.toLocalDate().atTime(23, 59, 59);

        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, Pageable.unpaged()))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        List<ReminderRespDTO> result = reminderService.getReminderByDeadline(user, searchDeadline);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reminderRespDTO, result.get(0));
        verify(reminderRepo).findByUserAndDeadlineBetween(user, startOfDay, endOfDay, Pageable.unpaged());
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetReminderByDeadlineWithNonExistingDateThenReturnEmptyList() {
        LocalDateTime searchDeadline = LocalDateTime.now();
        LocalDateTime startOfDay = searchDeadline.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = searchDeadline.toLocalDate().atTime(23, 59, 59);

        Page<Reminder> emptyPage = new PageImpl<>(Collections.emptyList());

        when(reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, Pageable.unpaged()))
                .thenReturn(emptyPage);

        List<ReminderRespDTO> result = reminderService.getReminderByDeadline(user, searchDeadline);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reminderRepo).findByUserAndDeadlineBetween(user, startOfDay, endOfDay, Pageable.unpaged());
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetUserRemindersBetweenDatesWithValidDatesThenReturnPageOfReminderRespDTO() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineBetween(user, startDate, endDate, pageable))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        Page<ReminderRespDTO> result = reminderService.getUserRemindersBetweenDates(user, startDate, endDate, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reminderRespDTO, result.getContent().get(0));

        verify(reminderRepo).findByUserAndDeadlineBetween(user, startDate, endDate, pageable);
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetUserRemindersBetweenDatesWithStartDateAfterEndDateThenThrowIllegalArgumentException() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(5);
        LocalDateTime endDate = LocalDateTime.now().minusDays(5);
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reminderService.getUserRemindersBetweenDates(user, startDate, endDate, pageable));
        assertEquals("Дата начала не может быть позже даты конца", exception.getMessage());
        verify(reminderRepo, never()).findByUserAndDeadlineBetween(any(), any(), any(), any());
    }

    @Test
    void whenGetUserRemindersBetweenDatesWithNullDatesThenReturnAllReminders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineBetween(user, null, null, pageable))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        Page<ReminderRespDTO> result = reminderService.getUserRemindersBetweenDates(user, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reminderRepo).findByUserAndDeadlineBetween(user, null, null, pageable);
    }

    @Test
    void whenGetUserExpiredRemindersWithExistingExpiredRemindersThenReturnPageOfReminderRespDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineBefore(eq(user), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        Page<ReminderRespDTO> result = reminderService.getUserExpiredReminders(user, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reminderRespDTO, result.getContent().get(0));

        verify(reminderRepo).findByUserAndDeadlineBefore(eq(user), any(LocalDateTime.class), eq(pageable));
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetUserExpiredRemindersWithNoExpiredRemindersThenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> emptyPage = Page.empty();

        when(reminderRepo.findByUserAndDeadlineBefore(eq(user), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(emptyPage);
        Page<ReminderRespDTO> result = reminderService.getUserExpiredReminders(user, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(reminderRepo).findByUserAndDeadlineBefore(eq(user), any(LocalDateTime.class), eq(pageable));
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetUserFutureRemindersWithExistingFutureRemindersThenReturnPageOfReminderRespDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineAfter(eq(user), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        Page<ReminderRespDTO> result = reminderService.getUserFutureReminders(user, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reminderRespDTO, result.getContent().get(0));
        verify(reminderRepo).findByUserAndDeadlineAfter(eq(user), any(LocalDateTime.class), eq(pageable));
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetUserFutureRemindersWithNoFutureRemindersThenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> emptyPage = Page.empty();  // Статический метод для создания пустой страницы

        when(reminderRepo.findByUserAndDeadlineAfter(eq(user), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<ReminderRespDTO> result = reminderService.getUserFutureReminders(user, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(reminderRepo).findByUserAndDeadlineAfter(eq(user), any(LocalDateTime.class), eq(pageable));
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetUserRemindersForDateWithExistingRemindersThenReturnPageOfReminderRespDTO() {
        LocalDateTime date = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        Page<Reminder> reminderPage = new PageImpl<>(List.of(reminder));

        when(reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, pageable))
                .thenReturn(reminderPage);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderRespDTO);

        Page<ReminderRespDTO> result = reminderService.getUserRemindersForDate(user, date, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reminderRespDTO, result.getContent().get(0));
        verify(reminderRepo).findByUserAndDeadlineBetween(user, startOfDay, endOfDay, pageable);
        verify(reminderMapper, times(1)).toDto(reminder);
    }

    @Test
    void whenGetUserRemindersForDateWithNoRemindersThenReturnEmptyPage() {
        LocalDateTime date = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        Page<Reminder> emptyPage = Page.empty();

        when(reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, pageable))
                .thenReturn(emptyPage);

        Page<ReminderRespDTO> result = reminderService.getUserRemindersForDate(user, date, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(reminderRepo).findByUserAndDeadlineBetween(user, startOfDay, endOfDay, pageable);
        verify(reminderMapper, never()).toDto(any(Reminder.class));
    }

    @Test
    void whenGetUserRemindersForDateWithNullDateThenThrowNullPointerException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(NullPointerException.class,
                () -> reminderService.getUserRemindersForDate(user, null, pageable));

        verify(reminderRepo, never()).findByUserAndDeadlineBetween(any(), any(), any(), any());
    }
}
