package com.example.lms.controller;

import com.example.lms.model.*;
import com.example.lms.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService issueService;
    private final BookService bookService;
    private final UserService userService;
    public IssueController(IssueService issueService, BookService bookService, UserService userService) {
        this.issueService = issueService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping("/issue")
    public ResponseEntity<?> issueBook(@RequestParam Long bookId, @RequestParam Long userId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        Book book = bookService.getAllBooks().stream().filter(b -> b.getId().equals(bookId)).findFirst().orElse(null);
        User user = userService.findById(userId).orElse(null);
        
        if (book == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book not found"));
        }
        
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        if (!book.isAvailable()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book is not available"));
        }
        
        // Check if user is borrowing for self (STUDENT role restriction)
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can only borrow books for themselves
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Students can only borrow books for themselves"));
            }
        }
        
        // Process the book issue
        book.setAvailable(false);
        bookService.addBook(book);
        IssueRecord record = issueService.issueBook(book, user);
        
        return ResponseEntity.ok(record);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserIssues(@PathVariable Long userId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        // Check if user is checking own records (STUDENT role restriction)
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can only see their own records
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Students can only view their own records"));
            }
        }
        
        return ResponseEntity.ok(issueService.getUserIssues(user));
    }

    @GetMapping
    public List<IssueRecord> getAllIssues() {
        return issueService.getAllIssues();
    }
    
    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestParam Long issueId, @RequestParam(required = false) Double finePaid) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        
        // Security checks
        if (currentUser == null) {
            return ResponseEntity.status(403).body(Map.of("error", "User not authenticated properly"));
        }
                
        IssueRecord record = issueService.findById(issueId).orElse(null);
        if (record == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Issue record not found: " + issueId));
        }
        
        // Students can only return their own books
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
                
        if (isStudent && !record.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Students can only return their own books",
                "issueUserId", record.getUser().getId(),
                "currentUserId", currentUser.getId()
            ));
        }
        
        // Process the return
        if (record.getReturnDate() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book already returned"));
        }
        
        double calculatedFine = issueService.calculateFine(record);
        // If no fine amount provided, use the calculated amount
        double actualFinePaid = (finePaid != null) ? finePaid : calculatedFine;
        
        record = issueService.returnBook(record, actualFinePaid);
        return ResponseEntity.ok(record);
    }
    
    @GetMapping("/fine/{issueId}")
    public ResponseEntity<?> calculateFine(@PathVariable Long issueId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        
        // Security checks
        if (currentUser == null) {
            return ResponseEntity.status(403).body(Map.of("error", "User not authenticated properly"));
        }
        
        IssueRecord record = issueService.findById(issueId).orElse(null);
        if (record == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Issue record not found"));
        }
        
        // Students can only calculate fines for their own books
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        
        if (isStudent && !record.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Students can only calculate fines for their own books",
                "issueUserId", record.getUser().getId(),
                "currentUserId", currentUser.getId()
            ));
        }
        
        double fine = issueService.calculateFine(record);
        return ResponseEntity.ok(Map.of(
            "issueId", record.getId(),
            "bookTitle", record.getBook().getTitle(),
            "userName", record.getUser().getName(),
            "dueDate", record.getDueDate().toString(),
            "daysOverdue", LocalDate.now().isAfter(record.getDueDate()) 
                ? ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now()) 
                : 0,
            "fineAmount", fine
        ));
    }
    
    // Reports and analytics endpoints
    
    @GetMapping("/reports/mostIssued")
    public ResponseEntity<?> getMostIssuedBooks() {
        // This endpoint is accessible to all authenticated users
        // We return the most issued books for the popular books feature
        
        // Get all issue records for popular books calculation
        List<IssueRecord> allIssues = issueService.getAllIssues();
        
        // Count issues by book
        Map<Book, Long> bookCounts = allIssues.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                IssueRecord::getBook, 
                java.util.stream.Collectors.counting()
            ));
        
        // Sort by count (most issued first) and convert to response format
        List<Map<String, Object>> result = bookCounts.entrySet().stream()
            .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
            .limit(10) // Top 10
            .map(entry -> {
                Book book = entry.getKey();
                Long count = entry.getValue();
                
                // Simplified response format directly containing book properties
                // This makes it easier to use in the frontend
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", book.getId());
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("available", book.isAvailable());
                bookData.put("issueCount", count);
                
                return bookData;
            })
            .collect(java.util.stream.Collectors.toList());
            
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/reports/userActivity/{userId}")
    public ResponseEntity<?> getUserActivityReport(@PathVariable Long userId) {
        // Only admin, librarian, or the user themselves can access their report
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        // Check if user exists
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        // Check if user is authorized to access this report
        if (!isAdminOrLibrarian && (currentUser == null || !currentUser.getId().equals(userId))) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        // Get user's issue history
        List<IssueRecord> userIssues = issueService.getUserIssues(user);
        
        // Calculate total fines
        double totalFines = userIssues.stream()
            .filter(issue -> issue.getFinePaid() != null)
            .mapToDouble(IssueRecord::getFinePaid)
            .sum();
            
        // Currently borrowed books
        List<Map<String, Object>> currentBorrows = userIssues.stream()
            .filter(issue -> issue.getReturnDate() == null)
            .map(issue -> Map.of(
                "issueId", issue.getId(),
                "book", Map.of(
                    "id", issue.getBook().getId(),
                    "title", issue.getBook().getTitle(),
                    "author", issue.getBook().getAuthor()
                ),
                "issueDate", issue.getIssueDate().toString(),
                "dueDate", issue.getDueDate().toString(),
                "daysOverdue", LocalDate.now().isAfter(issue.getDueDate()) 
                    ? ChronoUnit.DAYS.between(issue.getDueDate(), LocalDate.now()) 
                    : 0
            ))
            .collect(java.util.stream.Collectors.toList());
            
        // Return the report
        return ResponseEntity.ok(Map.of(
            "user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "username", user.getUsername(),
                "role", user.getRole()
            ),
            "totalBooksIssued", userIssues.size(),
            "totalCurrentlyBorrowed", currentBorrows.size(),
            "totalFinesPaid", totalFines,
            "currentBorrows", currentBorrows
        ));
    }
    
    @GetMapping("/reports/fines")
    public ResponseEntity<?> getFinesReport() {
        // Only admin and librarian can access reports
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can access reports"));
        }
        
        // Get all issue records
        List<IssueRecord> allIssues = issueService.getAllIssues();
        
        // Calculate total fines collected
        double totalFinesCollected = allIssues.stream()
            .filter(issue -> issue.getFinePaid() != null)
            .mapToDouble(IssueRecord::getFinePaid)
            .sum();
            
        // Calculate fines by month (for the current year)
        Map<Integer, Double> finesByMonth = allIssues.stream()
            .filter(issue -> issue.getFinePaid() != null && issue.getReturnDate() != null)
            .filter(issue -> issue.getReturnDate().getYear() == LocalDate.now().getYear())
            .collect(java.util.stream.Collectors.groupingBy(
                issue -> issue.getReturnDate().getMonthValue(),
                java.util.stream.Collectors.summingDouble(IssueRecord::getFinePaid)
            ));
            
        // Return the report
        return ResponseEntity.ok(Map.of(
            "totalFinesCollected", totalFinesCollected,
            "finesByMonth", finesByMonth,
            "outstandingFines", allIssues.stream()
                .filter(issue -> issue.getReturnDate() == null && LocalDate.now().isAfter(issue.getDueDate()))
                .mapToDouble(issueService::calculateFine)
                .sum()
        ));
    }
}