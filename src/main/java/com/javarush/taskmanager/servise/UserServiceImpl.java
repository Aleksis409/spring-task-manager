package com.javarush.taskmanager.servise;

import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }

        User user = new User(
                username,
                passwordEncoder.encode(password),
                UserRole.ROLE_USER
        );

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        User user = getById(userId);
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Override
    public void changeRole(Long userId, UserRole role) {
        User user = getById(userId);
        user.changeRole(role);
    }
}
