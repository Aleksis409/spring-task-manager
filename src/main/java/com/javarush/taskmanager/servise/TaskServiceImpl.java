package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.enums.TaskStatus;
import com.javarush.taskmanager.model.dto.TaskRequestDto;
import com.javarush.taskmanager.model.dto.TaskResponseDto;
import com.javarush.taskmanager.model.dto.TaskStatisticsResponse;
import com.javarush.taskmanager.model.entity.Task;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.model.mapper.TaskMapper;
import com.javarush.taskmanager.repository.TaskRepository;
import com.javarush.taskmanager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository,
                           TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        User currentUser = getCurrentUser();

        return taskRepository.findAllByOwner(currentUser)
                .stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = getTaskOrThrow(id);
        return taskMapper.toResponseDto(task);
    }

    @Override
    public TaskResponseDto createTask(TaskRequestDto request) {
        User currentUser = getCurrentUser();

        Task task = taskMapper.toEntity(request, currentUser);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto request) {
        Task task = getTaskOrThrow(id);

        taskMapper.updateEntity(task, request);

        return taskMapper.toResponseDto(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskOrThrow(id);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> filterTasks(String status,
                                             String fromDeadline,
                                             String toDeadline) {

        User currentUser = getCurrentUser();

        TaskStatus taskStatus =
                status != null ? TaskStatus.valueOf(status) : null;

        LocalDate from =
                fromDeadline != null ? LocalDate.parse(fromDeadline) : null;

        LocalDate to =
                toDeadline != null ? LocalDate.parse(toDeadline) : null;

        return taskRepository.filterTasks(currentUser, taskStatus, from, to)
                .stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatisticsResponse getStatistics() {
        User currentUser = getCurrentUser();

        long total = taskRepository.countByOwner(currentUser);
        long completed =
                taskRepository.countByOwnerAndStatus(currentUser, TaskStatus.COMPLETED);

        return new TaskStatisticsResponse(total, completed);
    }

    private Task getTaskOrThrow(Long id) {
        User currentUser = getCurrentUser();

        return taskRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private User getCurrentUser() {
        // TODO replace with SecurityContext + JWT
        return userRepository.findByUsername("test")
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}