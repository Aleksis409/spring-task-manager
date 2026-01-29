package com.javarush.taskmanager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
    private String picture;
}
