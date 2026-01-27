package com.javarush.taskmanager.config;

import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class TestAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = new User(
                "admin",
                passwordEncoder.encode("password"),
                UserRole.ROLE_ADMIN
        );

        userRepository.save(admin);
        log.info("TEST ADMIN created");
    }
}

