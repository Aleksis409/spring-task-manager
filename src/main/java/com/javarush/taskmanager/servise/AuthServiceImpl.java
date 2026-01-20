package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.model.dto.UserRegistrationRequest;
import com.javarush.taskmanager.model.dto.UserRegistrationResponse;
import com.javarush.taskmanager.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest request) {

        log.info("User registration started: username={}", request.getUsername());

        User user = userService.register(
                request.getUsername(),
                request.getPassword()
        );

        log.info("User registration completed: id={}, username={}",
                user.getId(), user.getUsername());

        return new UserRegistrationResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}

