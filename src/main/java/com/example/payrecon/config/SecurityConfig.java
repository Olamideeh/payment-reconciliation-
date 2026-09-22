package com.example.payrecon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Public endpoint
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Operations Officer actions
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reconciliations/upload",
                                "/api/reconciliations/*/run"
                        ).hasRole("OPERATIONS_OFFICER")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/investigation-cases/*/start",
                                "/api/investigation-cases/*/submit"
                        ).hasRole("OPERATIONS_OFFICER")

                        // Admin actions
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/investigation-cases/*/approve",
                                "/api/investigation-cases/*/reject"
                        ).hasRole("ADMIN")

                        // Both users can view information
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reconciliations/**",
                                "/api/investigation-cases/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "OPERATIONS_OFFICER"
                        )
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/audit-logs/**"
                        ).hasRole("ADMIN")

                        // Every other endpoint requires login
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}