package com.javarush.taskmanager.service.iml;

import com.javarush.taskmanager.enums.UserRole;
import com.javarush.taskmanager.exception.BusinessException;
import com.javarush.taskmanager.model.entity.User;
import com.javarush.taskmanager.repository.UserRepository;
import com.javarush.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String password) {
        return registerOAuth2User(username, password, username, null, null, "local", null);
    }

    /**
     * Registers a new user with the given username and password.
     * For internal/local registration only.
     */
    public User register(String username, String password, String name) {
        return registerOAuth2User(username, password, username, name, null, "local", null);
    }

    /**
     * Registers a new user via OAuth2 provider.
     * Creates user with generated password and OAuth2 metadata.
     */
    public User registerOAuth2User(String username, String email, String name,
                                   String picture, String provider, String providerId) {
        String password = passwordEncoder.encode(generateRandomPassword());
        return registerOAuth2User(username, password, email, name, picture, provider, providerId);
    }

    /**
     * Main registration method that handles both local and OAuth2 users.
     */
    private User registerOAuth2User(String username, String encodedPassword, String email,
                                    String name, String picture, String provider, String providerId) {
        log.debug("Attempt to register user: username={}, provider={}, email={}",
                username, provider, email);

        if ("local".equals(provider) && userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username already exists [{}]", username);
            throw new BusinessException("Username already exists");
        }

        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists [{}]", email);
            throw new BusinessException("Email already registered");
        }

        if (providerId != null && !providerId.isEmpty() &&
                userRepository.findByProviderId(providerId).isPresent()) {
            log.warn("Registration failed: providerId already exists [{}]", providerId);
            throw new BusinessException("User already registered with this provider");
        }

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email != null ? email : username)
                .name(name != null ? name : username)
                .picture(picture)
                .role(UserRole.ROLE_USER)
                .authProvider(provider)
                .providerId(providerId)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: id={}, username={}, provider={}, email={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getAuthProvider(),
                savedUser.getEmail());

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        log.debug("Fetching user by username={}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found by username={}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
    }

    /**
     * Retrieves user by email address.
     * Primarily used for OAuth2 authentication.
     */
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        log.debug("Fetching user by email={}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found by email={}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    /**
     * Retrieves user by OAuth2 provider ID.
     * Used to check if user already exists with the same OAuth2 account.
     */
    @Transactional(readOnly = true)
    public Optional<User> getByProviderId(String providerId) {
        log.debug("Fetching user by providerId={}", providerId);
        return userRepository.findByProviderId(providerId);
    }

    /**
     * Finds or creates user from OAuth2 attributes.
     * Used in OAuth2 authentication flow.
     */
    @Transactional
    public User findOrCreateOAuth2User(String email, String name, String picture,
                                       String provider, String providerId) {
        log.debug("Find or create OAuth2 user: email={}, provider={}, providerId={}",
                email, provider, providerId);

        Optional<User> userByProviderId = userRepository.findByProviderId(providerId);
        if (userByProviderId.isPresent()) {
            log.debug("Found existing user by providerId: {}", providerId);
            User user = userByProviderId.get();

            if (!Objects.equals(user.getName(), name) || !Objects.equals(user.getPicture(), picture)) {
                user.updateOAuth2Info(name, picture, providerId);
                userRepository.save(user);
                log.debug("Updated OAuth2 user info: {}", email);
            }
            return user;
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            log.debug("Found existing user by email: {}", email);
            User user = userByEmail.get();
            user.updateOAuth2Info(name, picture, providerId);
            user.setAuthProvider(provider);
            userRepository.save(user);
            log.debug("Updated existing user with OAuth2 provider: {}", email);
            return user;
        }

        log.debug("Creating new OAuth2 user: email={}", email);
        return registerOAuth2User(email, email, name, picture, provider, providerId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        log.debug("Fetching user by id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found by id={}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        log.info("Changing password for userId={}", userId);

        User user = getById(userId);

        if ("google".equals(user.getAuthProvider())) {
            log.warn("Attempt to change password for OAuth2 user: userId={}", userId);
            throw new BusinessException("OAuth2 users cannot change password directly");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for userId={}", userId);
    }

    @Override
    public void changeRole(Long userId, UserRole role) {
        log.info("Changing role for userId={} to {}", userId, role);

        User user = getById(userId);
        user.changeRole(role);
        userRepository.save(user);

        log.info("Role changed successfully for userId={}", userId);
    }

    /**
     * Generates a random password for OAuth2 users.
     * OAuth2 users don't need to know their password as they authenticate via provider.
     */
    private String generateRandomPassword() {
        String randomPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        log.debug("Generated random password for OAuth2 user");
        return randomPassword;
    }

    /**
     * Checks if email is already registered.
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Updates user's OAuth2 information.
     */
    public void updateOAuth2Info(Long userId, String name, String picture, String providerId) {
        log.info("Updating OAuth2 info for userId={}", userId);

        User user = getById(userId);
        user.updateOAuth2Info(name, picture, providerId);
        userRepository.save(user);

        log.info("OAuth2 info updated for userId={}", userId);
    }
}