package com.reminder.controllers;

import com.reminder.model.dto.in.ReminderReqDTO;
import com.reminder.model.dto.out.ReminderRespDTO;
import com.reminder.model.entities.User;
import com.reminder.model.service.ReminderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reminder")
@AllArgsConstructor
@Slf4j
public class ReminderController {
    private final ReminderService reminderService;
    @GetMapping()
    public Page<ReminderRespDTO> getReminders(@ModelAttribute("currentUser") User user,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "deadline") String sortBy,
                                             @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reminderService.getAllUserReminders(user, pageable);
    }

    @PostMapping()
    public ResponseEntity<ReminderRespDTO> createReminder(@ModelAttribute("currentUser") User user, @Valid @RequestBody ReminderReqDTO reminder) {
        return reminderService.createReminderForUser(user, reminder)
                .map(createdReminder -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(createdReminder))
                .orElse(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @GetMapping("/{reminderId}")
    public ResponseEntity<ReminderRespDTO> getReminder(@ModelAttribute("currentUser") User user, @PathVariable Long reminderId) {
        return reminderService.getUserReminder(user, reminderId)
                .map(reminder -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(reminder))
                .orElse(ResponseEntity
                        .notFound()
                        .build());
    }
    @PutMapping()
    public ResponseEntity<ReminderRespDTO> updateReminder(@ModelAttribute("currentUser") User user, @RequestBody ReminderReqDTO reminder) {
        return reminderService.updateUserReminder(user, reminder)
                .map(updatedReminder -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(updatedReminder))
                .orElse(ResponseEntity
                        .notFound()
                        .build());
    }
    @DeleteMapping("/{reminderId}")
    public void deleteReminder(@ModelAttribute("currentUser") User user, @PathVariable Long reminderId) {
           reminderService.deleteReminder(user, reminderId);
    }
    @GetMapping("/search/by-title/{title}")
    public ResponseEntity<List<ReminderRespDTO>> getReminderByTitle(@ModelAttribute("currentUser") User user, @PathVariable String title) {
        return ResponseEntity.ok(reminderService.getReminderByTitle(user, title));
    }
    @GetMapping("/search/by-description/{description}")
    public ResponseEntity<List<ReminderRespDTO>> getReminderByDescription(@ModelAttribute("currentUser") User user, @PathVariable String description) {
        return ResponseEntity.ok(reminderService.getReminderByDescription(user, description));
    }
    @GetMapping("/search/by-deadline/{deadline}")
    public ResponseEntity<List<ReminderRespDTO>> getReminderByDeadline(@ModelAttribute("currentUser") User user, @PathVariable LocalDateTime deadline) {
        return ResponseEntity.ok(reminderService.getReminderByDeadline(user, deadline));
    }
    @GetMapping("/filter/range")
    public Page<ReminderRespDTO> getRemindersByDateRange(
            @ModelAttribute("currentUser") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reminderService.getUserRemindersBetweenDates(user, startDate, endDate, pageable);
    }

    @GetMapping("/filter/expired")
    public Page<ReminderRespDTO> getExpiredReminders(
            @ModelAttribute("currentUser") User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reminderService.getUserExpiredReminders(user, pageable);
    }

    @GetMapping("/filter/future")
    public Page<ReminderRespDTO> getFutureReminders(
            @ModelAttribute("currentUser") User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reminderService.getUserFutureReminders(user, pageable);
    }

    @GetMapping("/filter/on-date")
    public Page<ReminderRespDTO> getRemindersOnDate(
            @ModelAttribute("currentUser") User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime dateTime = date.atStartOfDay();
        return reminderService.getUserRemindersForDate(user, dateTime, pageable);
    }

    @GetMapping("/filter/today")
    public Page<ReminderRespDTO> getTodayReminders(
            @ModelAttribute("currentUser") User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reminderService.getUserRemindersForDate(user, LocalDateTime.now(), pageable);
    }
}
