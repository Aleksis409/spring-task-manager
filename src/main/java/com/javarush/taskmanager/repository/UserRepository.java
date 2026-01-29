package com.javarush.taskmanager.repository;

import com.javarush.taskmanager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     * Used for authentication, authorization, and registration validation.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user with the given username exists.
     * Primarily used during registration to prevent duplicate usernames.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Finds a user by email address.
     * Used for OAuth2 authentication and user lookup by email.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email exists.
     * Used to prevent duplicate email registration, especially for OAuth2 users.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by OAuth2 provider-specific identifier.
     * Used to identify existing users logging in via the same OAuth2 provider.
     *
     * @param providerId the unique identifier provided by the OAuth2 provider
     * @return an Optional containing the user if found
     */
    Optional<User> findByProviderId(String providerId);
}