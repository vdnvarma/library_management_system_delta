package com.example.lms.controller;

import com.example.lms.model.*;
import com.example.lms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueBooks() {
        // Only admin and librarian can access reports
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can access reports"));
        }
        
        LocalDate today = LocalDate.now();
        List<IssueRecord> allIssues = issueService.getAllIssues();
        
        List<Map<String, Object>> overdueBooks = allIssues.stream()
            .filter(issue -> issue.getReturnDate() == null && issue.getDueDate().isBefore(today))
            .map(issue -> {
                Map<String, Object> details = new HashMap<>();
                details.put("issueId", issue.getId());
                details.put("bookTitle", issue.getBook().getTitle());
                details.put("bookId", issue.getBook().getId());
                details.put("userName", issue.getUser().getName());
                details.put("userId", issue.getUser().getId());
                details.put("issueDate", issue.getIssueDate().toString());
                details.put("dueDate", issue.getDueDate().toString());
                details.put("daysOverdue", issue.getDueDate().until(today).getDays());
                details.put("estimatedFine", issue.getDueDate().until(today).getDays() * 1.0); // $1 per day
                return details;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(overdueBooks);
    }
    
    @GetMapping("/popular-books")
    public ResponseEntity<?> getPopularBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Only admin and librarian can access reports
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can access reports"));
        }
        
        // Set default dates if not provided
        final LocalDate finalStartDate = (startDate == null) ? LocalDate.now().minusMonths(3) : startDate;
        final LocalDate finalEndDate = (endDate == null) ? LocalDate.now() : endDate;
        
        List<IssueRecord> allIssues = issueService.getAllIssues();
        
        // Group issues by book and count
        Map<Book, Long> bookCounts = allIssues.stream()
            .filter(issue -> !issue.getIssueDate().isBefore(finalStartDate) && !issue.getIssueDate().isAfter(finalEndDate))
            .collect(Collectors.groupingBy(IssueRecord::getBook, Collectors.counting()));
        
        // Sort by count and convert to response format
        List<Map<String, Object>> popularBooks = bookCounts.entrySet().stream()
            .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                Map<String, Object> details = new HashMap<>();
                details.put("bookId", entry.getKey().getId());
                details.put("title", entry.getKey().getTitle());
                details.put("author", entry.getKey().getAuthor());
                details.put("issueCount", entry.getValue());
                return details;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(popularBooks);
    }
    
    @GetMapping("/user-activity")
    public ResponseEntity<?> getUserActivity(@RequestParam Long userId) {
        // Only admin and librarian can access reports
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can access reports"));
        }
        
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        List<IssueRecord> userIssues = issueService.getUserIssues(user);
        List<Reservation> userReservations = reservationService.getUserReservations(user);
        
        Map<String, Object> activityReport = new HashMap<>();
        activityReport.put("userId", user.getId());
        activityReport.put("userName", user.getName());
        activityReport.put("currentIssues", userIssues.stream()
                .filter(issue -> issue.getReturnDate() == null)
                .count());
        activityReport.put("totalIssues", userIssues.size());
        activityReport.put("activeReservations", userReservations.stream()
                .filter(Reservation::isActive)
                .count());
        activityReport.put("overdueBooks", userIssues.stream()
                .filter(issue -> issue.getReturnDate() == null && issue.getDueDate().isBefore(LocalDate.now()))
                .count());
        
        // Calculate total fines
        double totalFines = userIssues.stream()
                .filter(issue -> issue.getReturnDate() == null && issue.getDueDate().isBefore(LocalDate.now()))
                .mapToDouble(issue -> issue.getDueDate().until(LocalDate.now()).getDays() * 1.0)
                .sum();
        activityReport.put("estimatedFines", totalFines);
        
        return ResponseEntity.ok(activityReport);
    }
}
