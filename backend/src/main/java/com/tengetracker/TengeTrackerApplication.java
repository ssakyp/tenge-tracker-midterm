package com.tengetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Users live in our own table and are checked by JwtAuthenticationFilter,
// so Spring Boot's default in-memory user (random password in the log) is disabled.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TengeTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TengeTrackerApplication.class, args);
    }
}
