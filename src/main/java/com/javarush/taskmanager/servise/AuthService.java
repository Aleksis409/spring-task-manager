package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.model.dto.AuthRequest;
import com.javarush.taskmanager.model.dto.AuthResponse;
import com.javarush.taskmanager.model.dto.UserRegistrationRequest;
import com.javarush.taskmanager.model.dto.UserRegistrationResponse;

public interface AuthService {

    UserRegistrationResponse register(UserRegistrationRequest request);
    AuthResponse login(AuthRequest request);
}