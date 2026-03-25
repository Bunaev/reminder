package com.reminder.model.dto;

import com.reminder.model.dto.in.ReminderReqDTO;
import com.reminder.model.dto.out.ReminderRespDTO;
import com.reminder.model.entities.Reminder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReminderMapper {
    @Mapping(source = "authorId", target = "user.id")
    Reminder toEntity(ReminderReqDTO dto);

    @Mapping(source = "user.id", target = "authorId")
    ReminderRespDTO toDto(Reminder reminder);
}
