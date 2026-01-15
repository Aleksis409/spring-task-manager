package com.javarush.taskmanager.model.dto;

import com.javarush.taskmanager.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TaskResponseDto {

    private final Long id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final LocalDate deadline;
}
