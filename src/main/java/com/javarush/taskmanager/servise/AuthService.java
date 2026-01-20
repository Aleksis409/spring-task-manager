package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.model.dto.UserRegistrationRequest;
import com.javarush.taskmanager.model.dto.UserRegistrationResponse;

public interface AuthService {

    UserRegistrationResponse register(UserRegistrationRequest request);
}