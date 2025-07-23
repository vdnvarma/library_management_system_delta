package com.example.lms.controller;

import com.example.lms.model.*;
import com.example.lms.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final BookService bookService;
    private final UserService userService;
    public ReservationController(ReservationService reservationService, BookService bookService, UserService userService) {
        this.reservationService = reservationService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveBook(@RequestParam Long bookId, @RequestParam Long userId) {
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
        
        if (book.isAvailable()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book is available. No need to reserve"));
        }
        
        // Check if user is reserving for self (STUDENT role restriction)
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can only reserve books for themselves
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Students can only reserve books for themselves"));
            }
        }
        
        // Process the reservation
        Reservation reservation = reservationService.reserveBook(book, user);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReservations(@PathVariable Long userId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        // Check if user is checking own reservations (STUDENT role restriction)
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can only see their own reservations
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Students can only view their own reservations"));
            }
        }
        
        return ResponseEntity.ok(reservationService.getUserReservations(user));
    }

    @GetMapping
    public ResponseEntity<?> getAllReservations() {
        // Only admin and librarian can see all reservations
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can view all reservations"));
        }
        
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        Reservation reservation = reservationService.getReservationById(id).orElse(null);
        if (reservation == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reservation not found"));
        }
        
        // Check permissions
        if (!isAdminOrLibrarian) {
            // Non-admin/librarian users can only cancel their own reservations
            User currentUser = userService.findByUsername(currentUsername).orElse(null);
            if (currentUser == null || !currentUser.getId().equals(reservation.getUser().getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "You can only cancel your own reservations"));
            }
        }
        
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(Map.of("message", "Reservation canceled successfully"));
    }
    
    @PostMapping("/notify-available/{id}")
    public ResponseEntity<?> notifyBookAvailable(@PathVariable Long id) {
        // Only admin and librarian can notify users
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can send notifications"));
        }
        
        Reservation reservation = reservationService.getReservationById(id).orElse(null);
        if (reservation == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reservation not found"));
        }
        
        // In a real application, you'd send an email or push notification here
        // For now, we'll just mark it as notified in the system
        reservation.setNotified(true);
        reservationService.save(reservation);
        
        return ResponseEntity.ok(Map.of(
            "message", "User " + reservation.getUser().getName() + " notified about book availability",
            "reservation", reservation
        ));
    }
} 