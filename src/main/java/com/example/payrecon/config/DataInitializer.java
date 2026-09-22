package com.example.payrecon.config;

import com.example.payrecon.entity.AppUser;
import com.example.payrecon.enums.UserRole;
import com.example.payrecon.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner createDevelopmentUsers() {

        return args -> {

            if (!appUserRepository.existsByUsername("admin.user")) {

                AppUser admin = new AppUser();
                admin.setUsername("admin.user");
                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);

                appUserRepository.save(admin);
            }

            if (!appUserRepository.existsByUsername("operations.officer")) {

                AppUser officer = new AppUser();
                officer.setUsername("operations.officer");
                officer.setPassword(
                        passwordEncoder.encode("Officer@123")
                );
                officer.setRole(UserRole.OPERATIONS_OFFICER);
                officer.setEnabled(true);

                appUserRepository.save(officer);
            }
        };
    }
}