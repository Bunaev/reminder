package com.reminder.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Data
@Table(name = "tbl_reminder")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name ="title", nullable = false, length = 50)
    private String title;
    @NotNull
    @Column(name = "description", nullable = false, length = 200)
    private String description;
    @CreationTimestamp
    @Column(name = "created", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "author", nullable = false)
    private User user;
    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;
    @Column(name = "email_send")
    private boolean emailSend = false;
    @Column(name = "telegram_send")
    private boolean telegramSend = false;
}
