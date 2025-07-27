package com.example.lms.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * This class initializes the application but does not create any default users.
 * Admin users are expected to be already present in the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    public DataInitializer() {
        // No dependencies needed since we're not creating admin users
    }

    @Override
    public void run(String... args) {
        // Admin users are already present in the database
        // No automatic creation of admin users
        System.out.println("Application initialized - using existing database users");
    }
}
