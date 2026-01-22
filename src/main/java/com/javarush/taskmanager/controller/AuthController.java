package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.model.dto.AuthRequest;
import com.javarush.taskmanager.model.dto.AuthResponse;
import com.javarush.taskmanager.model.dto.UserRegistrationRequest;
import com.javarush.taskmanager.model.dto.UserRegistrationResponse;
import com.javarush.taskmanager.servise.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @RequestBody @Valid UserRegistrationRequest request) {

        log.info("HTTP POST /api/auth/register username={}", request.getUsername());

        UserRegistrationResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid AuthRequest request) {

        log.info("HTTP POST /api/auth/login username={}", request.getUsername());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
