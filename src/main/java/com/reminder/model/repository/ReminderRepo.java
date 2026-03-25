package com.reminder.model.repository;

import com.reminder.model.entities.Reminder;
import com.reminder.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ReminderRepo extends JpaRepository<Reminder, Long> {
Page<Reminder> findByUser(User user, Pageable pageable);
List<Reminder> findByDescriptionContainingIgnoreCaseAndUserId(String description, Long userId);
List<Reminder> findByTitleContainingIgnoreCaseAndUserId(String title, Long userId);
Page<Reminder> findByUserAndDeadlineBetween(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
Page<Reminder> findByUserAndDeadlineBefore(
            User user,
            LocalDateTime date,
            Pageable pageable
    );
Page<Reminder> findByUserAndDeadlineAfter(
            User user,
            LocalDateTime date,
            Pageable pageable
    );
    @Query("SELECT r FROM Reminder r WHERE r.deadline <= :now AND " +
            "((r.user.email IS NOT NULL AND r.emailSend = false) OR " +
            "(r.user.telegram IS NOT NULL AND r.telegramSend = false))")
    List<Reminder> findPendingNotifications(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Reminder r WHERE r.id = :id AND r.user.id = :authorId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("authorId") Long authorId);
}
