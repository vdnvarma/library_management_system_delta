package com.example.lms.controller;

import com.example.lms.model.Book;
import com.example.lms.model.IssueRecord;
import com.example.lms.model.Reservation;
import com.example.lms.model.User;
import com.example.lms.service.BookService;
import com.example.lms.service.IssueService;
import com.example.lms.service.ReservationService;
import com.example.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This controller handles legacy URL patterns without the /api prefix.
 * It provides backward compatibility for older clients.
 */
@RestController
public class LegacyUrlController {

    private final BookService bookService;
    private final IssueService issueService;
    private final ReservationService reservationService;
    private final UserService userService;
    
    @Autowired
    public LegacyUrlController(BookService bookService, IssueService issueService, 
                              ReservationService reservationService, UserService userService) {
        this.bookService = bookService;
        this.issueService = issueService;
        this.reservationService = reservationService;
        this.userService = userService;
    }
    
    // ========== Book endpoints without /api prefix ==========
    
    @GetMapping("/books")
    public List<Book> getAllBooks() { 
        System.out.println("Legacy endpoint called: GET /books");
        return bookService.getAllBooks(); 
    }
    
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        System.out.println("Legacy endpoint called: GET /books/" + id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }
    
    @GetMapping("/books/search")
    public List<Book> searchBooks(@RequestParam(required = false) String query) {
        System.out.println("Legacy endpoint called: GET /books/search?query=" + query);
        if (query == null || query.isEmpty()) {
            return bookService.getAllBooks();
        }
        // Default to searching by title if no type is specified
        return bookService.searchByTitle(query);
    }
    
    // ========== Issue endpoints without /api prefix ==========
    
    @GetMapping("/issues")
    public List<IssueRecord> getAllIssues() {
        System.out.println("Legacy endpoint called: GET /issues");
        return issueService.getAllIssues();
    }
    
    @GetMapping("/issues/user/{userId}")
    public List<IssueRecord> getIssuesByUserId(@PathVariable Long userId) {
        System.out.println("Legacy endpoint called: GET /issues/user/" + userId);
        // First get the user by ID
        var userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + userId);
            return List.of();
        }
        return issueService.getUserIssues(userOpt.get());
    }
    
    @GetMapping("/issues/{id}")
    public ResponseEntity<?> getIssueById(@PathVariable Long id) {
        System.out.println("Legacy endpoint called: GET /issues/" + id);
        var issueOpt = issueService.findById(id);
        return issueOpt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // ========== Reservation endpoints without /api prefix ==========
    
    @GetMapping("/reservations")
    public List<Reservation> getAllReservations() {
        System.out.println("Legacy endpoint called: GET /reservations");
        return reservationService.getAllReservations();
    }
    
    @GetMapping("/reservations/user/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
        System.out.println("Legacy endpoint called: GET /reservations/user/" + userId);
        // First get the user by ID
        var userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + userId);
            return List.of();
        }
        return reservationService.getUserReservations(userOpt.get());
    }
    
    @PostMapping("/reservations/reserve")
    public ResponseEntity<?> reserveBook(@RequestParam Long bookId, @RequestParam Long userId) {
        System.out.println("Legacy endpoint called: POST /reservations/reserve");
        
        var bookOpt = bookService.getBookById(bookId);
        var userOpt = userService.findById(userId);
        
        if (bookOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Book not found");
        }
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        Reservation reservation = reservationService.reserveBook(bookOpt.get(), userOpt.get());
        return ResponseEntity.ok(reservation);
    }
}
