package com.javarush.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.exception.handler.GlobalExceptionHandler;
import com.javarush.taskmanager.model.dto.*;
import com.javarush.taskmanager.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserRegistrationRequest registrationRequest;
    private UserRegistrationResponse registrationResponse;
    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private LogoutRequest logoutRequest;

    private final Long userId = 1L;
    private final String username = "testuser";
    private final String password = "password123";
    private final String accessToken = "access.token.here";
    private final String refreshToken = "refresh.token.here";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        registrationRequest = new UserRegistrationRequest(username, password);
        registrationResponse = new UserRegistrationResponse(userId, username, UserRole.ROLE_USER);

        authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        authResponse = new AuthResponse(accessToken, refreshToken);

        logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);
    }

    @Test
    void register_ShouldCreateUserAndReturnResponse() throws Exception {
        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(registrationResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ShouldValidateRequest() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest("", password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("username")))
                .andExpect(jsonPath("$[0].message", notNullValue()));

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldHandleUsernameAlreadyExists() throws Exception {
        when(authService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalStateException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Username already exists")));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ShouldHandleOtherExceptions() throws Exception {
        when(authService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Internal server error: Database error")));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void login_ShouldReturnTokensOnSuccessfulAuthentication() throws Exception {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(accessToken)))
                .andExpect(jsonPath("$.refreshToken", is(refreshToken)));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    void login_ShouldValidateRequest() throws Exception {
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword(password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("username")))
                .andExpect(jsonPath("$[0].message", notNullValue()));

        verify(authService, never()).login(any());
    }

    @Test
    void login_ShouldHandleInvalidCredentials() throws Exception {
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Internal server error: Invalid credentials")));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    void login_ShouldHandleAuthenticationFailure() throws Exception {
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Internal server error: Authentication failed")));

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    void logout_ShouldRevokeRefreshToken() throws Exception {
        doNothing().when(authService).logout(refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logout(refreshToken);
    }

    @Test
    void logout_ShouldHandleMissingRefreshToken() throws Exception {
        LogoutRequest invalidRequest = new LogoutRequest();
        invalidRequest.setRefreshToken(null);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_ShouldHandleInvalidToken() throws Exception {
        String invalidToken = "invalid.token";
        LogoutRequest invalidRequest = new LogoutRequest();
        invalidRequest.setRefreshToken(invalidToken);

        doThrow(new RuntimeException("Invalid token")).when(authService).logout(invalidToken);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Internal server error: Invalid token")));

        verify(authService, times(1)).logout(invalidToken);
    }

    @Test
    void register_ShouldHandleVeryLongUsername() throws Exception {
        String longUsername = "u".repeat(100);
        UserRegistrationRequest longRequest = new UserRegistrationRequest(longUsername, password);
        UserRegistrationResponse longResponse = new UserRegistrationResponse(2L, longUsername, UserRole.ROLE_USER);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(longResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(longUsername)));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ShouldHandleVeryLongPassword() throws Exception {
        String longPassword = "p".repeat(255);
        UserRegistrationRequest longRequest = new UserRegistrationRequest(username, longPassword);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(registrationResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void login_ShouldHandleVeryLongCredentials() throws Exception {
        String longUsername = "u".repeat(1000);
        String longPassword = "p".repeat(1000);

        AuthRequest longRequest = new AuthRequest();
        longRequest.setUsername(longUsername);
        longRequest.setPassword(longPassword);

        AuthResponse response = new AuthResponse("token", "refresh");
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longRequest)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login(any(AuthRequest.class));
    }

    @Test
    void register_ShouldHandleSpecialCharactersInUsername() throws Exception {
        String specialUsername = "user@email.com+test";
        UserRegistrationRequest specialRequest = new UserRegistrationRequest(specialUsername, password);
        UserRegistrationResponse specialResponse = new UserRegistrationResponse(3L, specialUsername, UserRole.ROLE_USER);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(specialResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(specialUsername)));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void fullAuthenticationFlow_ShouldWork() throws Exception {
        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(registrationResponse);
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);
        doNothing().when(authService).logout(refreshToken);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(username)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(accessToken)));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
        verify(authService, times(1)).login(any(AuthRequest.class));
        verify(authService, times(1)).logout(refreshToken);
    }

    @Test
    void register_ShouldReturnLocationHeaderIfImplemented() throws Exception {
        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(registrationResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userId.intValue())));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ShouldHandleNullRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Request body is required and must be valid JSON")));

        verify(authService, never()).register(any());
    }

    @Test
    void login_ShouldHandleNullRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Request body is required and must be valid JSON")));

        verify(authService, never()).login(any());
    }

    @Test
    void logout_ShouldHandleNullRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Request body is required and must be valid JSON")));

        verify(authService, never()).logout(any());
    }

    @Test
    void register_ShouldHandleInvalidJsonFormat() throws Exception {
        String invalidJson = "{username: \"test\", password: \"pass\"}"; // без кавычек у ключей

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Request body is required and must be valid JSON")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_ShouldHandleWrongJsonType() throws Exception {
        String arrayJson = "[\"test\", \"pass\"]";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(arrayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Request body is required and must be valid JSON")));

        verify(authService, never()).register(any());
    }
}