package com.example.lms.service;

import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepo;
    public UserService(UserRepository userRepo) { this.userRepo = userRepo; }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    
    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }
    
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
    
    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }
    
    public User save(User user) { return userRepo.save(user); }
    
    public List<User> findByRole(com.example.lms.model.Role role) {
        return userRepo.findByRole(role);
    }
} 