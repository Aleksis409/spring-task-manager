package com.javarush.taskmanager.security;

import com.javarush.taskmanager.security.jwt.JwtAuthenticationFilter;
import com.javarush.taskmanager.security.oauth.CustomOAuth2UserService;
import com.javarush.taskmanager.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final Environment environment;

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/prometheus").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (isGoogleOAuth2Configured()) {
            log.info("Google OAuth2 is configured, enabling OAuth2 login");
            configureOAuth2Login(http);
        } else {
            log.warn("Google OAuth2 is not configured. To enable OAuth2 login, set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
        }

        return http.build();
    }

    private boolean isGoogleOAuth2Configured() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret");

        boolean isConfigured = clientId != null &&
                !clientId.isEmpty() &&
                !clientId.equals("YOUR_GOOGLE_CLIENT_ID") &&
                !clientId.equals("your-client-id-here") &&
                clientSecret != null &&
                !clientSecret.isEmpty() &&
                !clientSecret.equals("YOUR_GOOGLE_CLIENT_SECRET") &&
                !clientSecret.equals("your-client-secret-here");

        log.debug("Google OAuth2 configured: {}", isConfigured);
        return isConfigured;
    }

    private void configureOAuth2Login(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/api/auth/oauth2/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 login failed: {}", exception.getMessage(), exception);

                    String errorMessage = "Authentication failed";
                    if (exception.getMessage().contains("invalid_client")) {
                        errorMessage = "Invalid OAuth2 configuration. Please contact administrator.";
                    } else if (exception.getMessage().contains("access_denied")) {
                        errorMessage = "Access denied by Google";
                    }

                    try {
                        response.sendRedirect("/api/auth/oauth2/error?message=" +
                                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        log.error("Failed to redirect to error page", e);
                    }
                })
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}