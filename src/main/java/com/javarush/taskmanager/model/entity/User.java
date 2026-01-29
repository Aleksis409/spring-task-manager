package com.javarush.taskmanager.model.entity;

import com.javarush.taskmanager.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_provider_id", columnList = "providerId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(length = 20)
    private String authProvider;

    @Column(length = 100)
    private String providerId;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = username;
        this.name = username;
        this.role = role;
        this.authProvider = "local";
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void updateOAuth2Info(String name, String picture, String providerId) {
        this.name = name;
        this.picture = picture;
        this.providerId = providerId;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }
}