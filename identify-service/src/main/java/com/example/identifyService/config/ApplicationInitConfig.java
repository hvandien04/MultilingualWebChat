package com.example.identifyService.config;

import com.example.identifyService.entity.User;
import com.example.identifyService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    private static String ADMIN_USERNAME = "admin";
    @NonFinal
    private static String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner init(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if(userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
                User user = User.builder()
                        .userId("admin")
                        .username(ADMIN_USERNAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email("Admin@localhost.com")
                        .role("ADMIN")
                        .build();
                userRepository.save(user);
                log.info("Admin user created successfully");
            }
            log.info("Application initialization completed .....");
        };
    }
}
