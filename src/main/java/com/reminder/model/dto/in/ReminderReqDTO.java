package com.reminder.model.dto.in;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReminderReqDTO {
    private Long id;
    @NotBlank(message = "Заголовок обязателен!")
    @Size(min = 2, max = 50, message = "Заголовок от 2 до 50 символов")
    private String title;
    @NotBlank(message = "Описание обязательно!")
    @Size(min = 2, max = 200, message = "Имя от 5 до 200 символов")
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long authorId;
    @Future(message = "Дедлайн должен быть в будущем")
    private LocalDateTime deadline;
}
