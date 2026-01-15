package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.model.dto.TaskRequestDto;
import com.javarush.taskmanager.model.dto.TaskResponseDto;
import com.javarush.taskmanager.servise.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /api/tasks
     *
     * Retrieves all tasks belonging to the currently authenticated user.
     *
     * @return list of user's tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        List<TaskResponseDto> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/tasks/{id}
     *
     * Retrieves a specific task by its ID.
     * Access is allowed only if the task belongs to the current user.
     *
     * @param id task identifier
     * @return task details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * POST /api/tasks
     *
     * Creates a new task for the currently authenticated user.
     *
     * @param request task creation data
     * @return created task
     */
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @RequestBody @Valid TaskRequestDto request) {

        TaskResponseDto createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * PUT /api/tasks/{id}
     *
     * Updates an existing task.
     * Only the owner of the task is allowed to perform this operation.
     *
     * @param id      task identifier
     * @param request updated task data
     * @return updated task
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @RequestBody @Valid TaskRequestDto request) {

        TaskResponseDto updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * DELETE /api/tasks/{id}
     *
     * Deletes a task by its ID.
     * Only the owner of the task is allowed to perform this operation.
     *
     * @param id task identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    /**
     * GET /api/tasks/filter
     *
     * Filters tasks by status and/or deadline range.
     * All filters are optional.
     *
     * @param status       task status
     * @param fromDeadline start of deadline range
     * @param toDeadline   end of deadline range
     * @return filtered list of tasks
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TaskResponseDto>> filterTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDeadline,
            @RequestParam(required = false) String toDeadline) {

        List<TaskResponseDto> tasks =
                taskService.filterTasks(status, fromDeadline, toDeadline);

        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/tasks/stats
     *
     * Returns task statistics for the currently authenticated user.
     *
     * @return task statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getStatistics());
    }
}
