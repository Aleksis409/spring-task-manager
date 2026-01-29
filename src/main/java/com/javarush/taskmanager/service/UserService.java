package com.javarush.taskmanager.service;

import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.model.entity.User;

import java.util.Optional;

public interface UserService {

    /**
     * Registers a new user with username and password (local registration).
     */
    User register(String username, String password);

    /**
     * Registers a new user with username, password, and display name.
     */
    User register(String username, String password, String name);

    /**
     * Retrieves user by username.
     */
    User getByUsername(String username);

    /**
     * Retrieves user by email address.
     */
    User getByEmail(String email);

    /**
     * Retrieves user by database ID.
     */
    User getById(Long id);

    /**
     * Finds user by OAuth2 provider ID.
     */
    Optional<User> getByProviderId(String providerId);

    /**
     * Finds existing user or creates new one from OAuth2 attributes.
     */
    User findOrCreateOAuth2User(String email, String name, String picture,
                                String provider, String providerId);

    /**
     * Changes user's password (only for local users).
     */
    void changePassword(Long userId, String newPassword);

    /**
     * Changes user's role.
     */
    void changeRole(Long userId, UserRole role);

    /**
     * Checks if email is already registered.
     */
    boolean emailExists(String email);

    /**
     * Updates OAuth2 user information.
     */
    void updateOAuth2Info(Long userId, String name, String picture, String providerId);
}
