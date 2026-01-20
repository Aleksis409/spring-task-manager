package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String username, String password) {

        log.debug("Attempt to register user: username={}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username already exists [{}]", username);
            throw new IllegalStateException("Username already exists");
        }

        User user = new User(
                username,
                passwordEncoder.encode(password),
                UserRole.ROLE_USER
        );

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: id={}, username={}",
                savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        log.debug("Fetching user by username={}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found by username={}", username);
                    return new RuntimeException("User not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        log.debug("Fetching user by id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found by id={}", id);
                    return new RuntimeException("User not found");
                });
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        log.info("Changing password for userId={}", userId);

        User user = getById(userId);
        user.changePassword(passwordEncoder.encode(newPassword));

        log.info("Password changed successfully for userId={}", userId);
    }

    @Override
    public void changeRole(Long userId, UserRole role) {
        log.info("Changing role for userId={} to {}", userId, role);

        User user = getById(userId);
        user.changeRole(role);

        log.info("Role changed successfully for userId={}", userId);
    }
}
