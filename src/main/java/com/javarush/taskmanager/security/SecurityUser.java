package com.javarush.taskmanager.security;

import com.javarush.taskmanager.model.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SecurityUser implements UserDetails, OAuth2User {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;
    private String nameAttributeKey;

    public SecurityUser(User user) {
        this.user = user;
        this.authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );
        this.attributes = new HashMap<>();
        this.nameAttributeKey = "username";
    }

    public SecurityUser(User user, Map<String, Object> attributes, String nameAttributeKey) {
        this.user = user;
        this.authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );
        this.attributes = attributes != null ? attributes : new HashMap<>();
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

