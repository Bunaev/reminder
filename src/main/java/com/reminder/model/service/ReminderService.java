package com.reminder.model.service;

import com.reminder.model.dto.ReminderMapper;
import com.reminder.model.dto.in.ReminderReqDTO;
import com.reminder.model.dto.out.ReminderRespDTO;
import com.reminder.model.entities.Reminder;
import com.reminder.model.entities.User;
import com.reminder.model.repository.ReminderRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final ReminderRepo reminderRepo;
    private final ReminderMapper reminderMapper;
    @Transactional
    public Optional<ReminderRespDTO> createReminderForUser(User user, ReminderReqDTO reminderReqDTO) {
        Reminder reminder = reminderMapper.toEntity(reminderReqDTO);
        reminder.setUser(user);
        return Optional.of(reminderMapper.toDto(reminderRepo.save(reminder)));
    }
    @Transactional(readOnly = true)
    public Optional<ReminderRespDTO> getUserReminder(User user, Long id) {
        return reminderRepo.findById(id)
                .filter(reminder -> reminder.getUser().getId().equals(user.getId()))
                .map(reminderMapper::toDto);
    }
    @Transactional
    public Optional<ReminderRespDTO> updateUserReminder(User user, ReminderReqDTO reminderReqDTO) {
        return reminderRepo.findById(reminderReqDTO.getId())
                .filter(reminder -> reminder.getUser().getId().equals(user.getId()))
                .map(reminder -> {
                    reminder.setTitle(reminderReqDTO.getTitle());
                    reminder.setDescription(reminderReqDTO.getDescription());
                    reminder.setDeadline(reminderReqDTO.getDeadline());
                    if (reminderReqDTO.getDeadline().isAfter(LocalDateTime.now())) {
                    reminder.setTelegramSend(false);
                    reminder.setEmailSend(false);
                    }
                    return reminderRepo.save(reminder);
                }).map(reminderMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<ReminderRespDTO> getAllUserReminders(User user, Pageable pageable) {
        return reminderRepo.findByUser(user, pageable).map(reminderMapper::toDto);
    }
    @Transactional
    public void deleteReminder(User user, Long id) {
        int deleted = reminderRepo.deleteByIdAndUserId(id, user.getId());
        if (deleted == 0) {
            throw new EntityNotFoundException("Напоминание не найдено или не принадлежит пользователю");
        }
    }
    @Transactional(readOnly = true)
    public List<ReminderRespDTO> getReminderByTitle(User user, String title) {
        return reminderRepo.findByTitleContainingIgnoreCaseAndUserId(title, user.getId())
                .stream()
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReminderRespDTO> getReminderByDescription(User user, String description) {
        return reminderRepo.findByDescriptionContainingIgnoreCaseAndUserId(description, user.getId())
                .stream()
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReminderRespDTO> getReminderByDeadline(User user, LocalDateTime deadline) {
        LocalDateTime startOfDay = deadline.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = deadline.toLocalDate().atTime(23, 59, 59);

        return reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, Pageable.unpaged())
                .stream()
                .map(reminderMapper::toDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Page<ReminderRespDTO> getUserRemindersBetweenDates(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты конца");
        }
        return reminderRepo.findByUserAndDeadlineBetween(user, startDate, endDate, pageable)
                .map(reminderMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<ReminderRespDTO> getUserExpiredReminders(
            User user,
            Pageable pageable) {
        return reminderRepo.findByUserAndDeadlineBefore(user, LocalDateTime.now(), pageable)
                .map(reminderMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<ReminderRespDTO> getUserFutureReminders(
            User user,
            Pageable pageable) {
        return reminderRepo.findByUserAndDeadlineAfter(user, LocalDateTime.now(), pageable)
                .map(reminderMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<ReminderRespDTO> getUserRemindersForDate(
            User user,
            LocalDateTime date,
            Pageable pageable) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        return reminderRepo.findByUserAndDeadlineBetween(user, startOfDay, endOfDay, pageable)
                .map(reminderMapper::toDto);
    }

}
