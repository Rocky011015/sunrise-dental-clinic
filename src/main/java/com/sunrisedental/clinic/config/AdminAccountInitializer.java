package com.sunrisedental.clinic.config;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.UserRole;
import com.sunrisedental.clinic.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminFullName;

    public AdminAccountInitializer(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.username:admin}") String adminUsername,
            @Value("${app.bootstrap.admin.password:}") String adminPassword,
            @Value("${app.bootstrap.admin.full-name:Clinic Administrator}")
            String adminFullName) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {

        if (!StringUtils.hasText(adminPassword)) {
            LOGGER.info(
                    "Administrator bootstrap skipped because ADMIN_PASSWORD is not configured."
            );
            return;
        }

        String username = adminUsername.trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            LOGGER.info(
                    "Administrator account '{}' already exists.",
                    username
            );
            return;
        }

        AppUser administrator = new AppUser(
                username,
                passwordEncoder.encode(adminPassword),
                adminFullName.trim(),
                UserRole.ADMIN,
                true
        );

        userRepository.save(administrator);

        LOGGER.info(
                "Administrator account '{}' created successfully.",
                username
        );
    }
}