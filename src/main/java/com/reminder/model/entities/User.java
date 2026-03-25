package com.reminder.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Data
@Table(name = "tbl_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name ="name", unique = true, nullable = false)
    private String name;
    @NotNull
    @Column(name = "password")
    private String password;
    @NotNull
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @NotNull
    @Column(name = "telegram", unique = true, nullable = false)
    private String telegram;
    @Column(name = "telegram_chat_id", unique = true)
    private Long telegramChatId;
    @OneToMany(mappedBy = "user")
    private List<Reminder> reminders;

}
