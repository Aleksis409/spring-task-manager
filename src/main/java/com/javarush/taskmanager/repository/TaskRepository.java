package com.javarush.taskmanager.repository;

import com.javarush.taskmanager.enums.TaskStatus;
import com.javarush.taskmanager.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.javarush.taskmanager.model.entity.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Returns all tasks belonging to a specific user.
     */
    List<Task> findAllByOwner(User owner);

    /**
     * Counts all tasks belonging to a specific user.
     */
    long countByOwner(User owner);

    /**
     * Counts completed tasks for a specific user.
     */
    long countByOwnerAndStatus(User owner, TaskStatus status);
}