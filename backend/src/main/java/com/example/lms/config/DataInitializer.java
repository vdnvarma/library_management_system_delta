package com.example.lms.config;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

/**
 * This class initializes the application and creates a default admin user
 * when the application starts if one doesn't exist already.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Value("${admin.create-on-startup:true}")
    private boolean createAdminOnStartup;
    
    @Value("${admin.username:admin}")
    private String adminUsername;
    
    @Value("${admin.password:admin123}")
    private String adminPassword;
    
    @Value("${admin.name:Admin User}")
    private String adminName;
    
    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("Application initialized" + 
                           (createAdminOnStartup ? " - creating default admin user if needed" : 
                                                  " - using existing database users"));
        
        // Create default admin if enabled
        if (createAdminOnStartup) {
            createAdminIfNotExists();
        }
    }
    
    private void createAdminIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setName(adminName);
            admin.setUsername(adminUsername);
            admin.setPassword(adminPassword);
            admin.setRole(Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("Created default admin user: " + adminUsername);
        } else {
            System.out.println("Admin user already exists, skipping creation");
        }
    }
}
