package com.example.wellnessbackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // ======================
    // PASSWORD ENCODER
    // ======================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================
    // AUTH MANAGER
    // ======================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ======================
    // SECURITY FILTER CHAIN
    // ======================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (JWT based auth)
                .csrf(csrf -> csrf.disable())

                // Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // ======================
                        // COMMON / ERROR / SWAGGER
                        // ======================
                        .requestMatchers(
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ======================
                        // AUTH (PUBLIC)
                        // ======================
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh-token"
                        ).permitAll()

                        // ======================
                        // PRACTITIONERS (PUBLIC VIEW)
                        // ======================
                        .requestMatchers(HttpMethod.GET,
                                "/api/practitioners/**"
                        ).permitAll()

                        // ======================
                        // PRODUCTS
                        // ======================
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/products"
                        ).hasAnyRole("ADMIN", "PRACTITIONER")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/products/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // ======================
                        // RECOMMENDATIONS
                        // ======================
                        .requestMatchers(HttpMethod.POST,
                                "/api/recommendations"
                        ).hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET,
                                "/api/recommendations/user/**"
                        ).hasAnyRole("PATIENT", "ADMIN")

                        // ======================
                        // EXTERNAL API (Milestone 4) - PUBLIC
                        // ======================
                        .requestMatchers(HttpMethod.GET,
                                "/external/**"
                        ).permitAll()

                        // ======================
                        // NOTIFICATIONS
                        // ======================
                        .requestMatchers(HttpMethod.POST,
                                "/api/notifications"
                        ).hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET,
                                "/api/notifications/**"
                        ).hasAnyRole("PATIENT", "ADMIN")

                        // ======================
                        // ADMIN ONLY
                        // ======================
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ======================
                        // EVERYTHING ELSE
                        // ======================
                        .anyRequest().authenticated()
                )

                // JWT Filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
