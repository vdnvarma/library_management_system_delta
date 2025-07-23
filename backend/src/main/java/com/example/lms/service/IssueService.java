package com.example.lms.service;

import com.example.lms.model.*;
import com.example.lms.repository.IssueRecordRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class IssueService {
    private final IssueRecordRepository issueRepo;
    private final double FINE_PER_DAY = 1.0; // $1 per day fine
    
    public IssueService(IssueRecordRepository issueRepo) { 
        this.issueRepo = issueRepo; 
    }

    public IssueRecord issueBook(Book book, User user) {
        IssueRecord ir = new IssueRecord();
        ir.setBook(book);
        ir.setUser(user);
        ir.setIssueDate(LocalDate.now());
        ir.setDueDate(LocalDate.now().plusDays(14));
        ir.setFinePaid(0.0); // Initialize fine as 0
        return issueRepo.save(ir);
    }
    
    public List<IssueRecord> getUserIssues(User user) {
        return issueRepo.findByUser(user);
    }
    
    public List<IssueRecord> getAllIssues() {
        return issueRepo.findAll();
    }
    
    public IssueRecord save(IssueRecord ir) { 
        return issueRepo.save(ir); 
    }
    
    public Optional<IssueRecord> findById(Long id) {
        return issueRepo.findById(id);
    }
    
    public double calculateFine(IssueRecord record) {
        if (record.getReturnDate() != null) {
            // Already returned
            return record.getFinePaid();
        }
        
        LocalDate today = LocalDate.now();
        if (today.isAfter(record.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
            return daysLate * FINE_PER_DAY;
        }
        
        return 0.0;
    }
    
    public IssueRecord returnBook(IssueRecord record, double finePaid) {
        record.setReturnDate(LocalDate.now());
        record.setFinePaid(finePaid);
        record.getBook().setAvailable(true);
        return save(record);
    }
} 