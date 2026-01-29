package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.model.dto.*;
import com.javarush.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final Environment environment;

    // Явный конструктор
    public AuthController(AuthService authService, Environment environment) {
        this.authService = authService;
        this.environment = environment;
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        log.info("HTTP POST /api/auth/refresh");
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // Эндпоинт для инициации OAuth2 логина
    @GetMapping("/oauth2/login")
    public ResponseEntity<?> oauth2Login() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");

        // Проверяем, настроен ли OAuth2
        if (clientId == null ||
                clientId.isEmpty() ||
                clientId.equals("YOUR_GOOGLE_CLIENT_ID") ||
                clientId.equals("your-client-id-here")) {

            log.warn("OAuth2 login attempt but Google OAuth2 is not configured");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "OAuth2 not available",
                            "message", "Google OAuth2 authentication is not configured. Please use traditional login."
                    ));
        }

        log.info("Redirecting to Google OAuth2 login");
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
                .build();
    }

    @GetMapping("/oauth2/status")
    public ResponseEntity<Map<String, Object>> getOAuth2Status() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        boolean isConfigured = clientId != null &&
                !clientId.isEmpty() &&
                !clientId.equals("YOUR_GOOGLE_CLIENT_ID") &&
                !clientId.equals("your-client-id-here");

        Map<String, Object> response = new HashMap<>();
        response.put("enabled", isConfigured);
        response.put("provider", "google");

        if (isConfigured) {
            response.put("message", "Google OAuth2 is configured");
            // Маскируем client-id для безопасности
            response.put("clientId", clientId.substring(0, Math.min(8, clientId.length())) + "..." + clientId.substring(clientId.length() - 4));
        } else {
            response.put("message", "Google OAuth2 is not configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/error")
    public ResponseEntity<Map<String, String>> oauth2Error(
            @RequestParam(value = "message", required = false) String message) {

        log.error("OAuth2 authentication error: {}", message);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "OAuth2 authentication failed");
        errorResponse.put("message", message != null ? message : "Unknown error");
        errorResponse.put("suggestion", "Try using traditional username/password login");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}
