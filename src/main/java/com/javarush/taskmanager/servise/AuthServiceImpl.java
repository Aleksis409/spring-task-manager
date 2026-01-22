package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.model.dto.AuthRequest;
import com.javarush.taskmanager.model.dto.AuthResponse;
import com.javarush.taskmanager.model.dto.UserRegistrationRequest;
import com.javarush.taskmanager.model.dto.UserRegistrationResponse;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.security.SecurityUser;
import com.javarush.taskmanager.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserService userService,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest request) {

        log.info("User registration started: username={}", request.getUsername());

        User user = userService.register(
                request.getUsername(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getUsername());

        log.info("User registration completed: id={}, username={}",
                user.getId(), user.getUsername());

        return new UserRegistrationResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("User login attempt: username={}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtService.generateToken(securityUser.getUsername());

        return new AuthResponse(token);
    }
}


