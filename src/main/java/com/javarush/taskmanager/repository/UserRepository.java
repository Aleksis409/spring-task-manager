package com.javarush.taskmanager.repository;

import com.javarush.taskmanager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
