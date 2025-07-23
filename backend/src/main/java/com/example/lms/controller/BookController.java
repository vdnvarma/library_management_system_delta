package com.example.lms.controller;

import com.example.lms.model.Book;
import com.example.lms.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    public BookController(BookService bookService) { this.bookService = bookService; }

    @GetMapping
    public List<Book> getAllBooks() { return bookService.getAllBooks(); }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String type, @RequestParam String keyword) {
        switch (type) {
            case "title": return bookService.searchByTitle(keyword);
            case "author": return bookService.searchByAuthor(keyword);
            case "isbn": return bookService.searchByIsbn(keyword);
            case "genre": return bookService.searchByGenre(keyword);
            default: return List.of();
        }
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) { 
        // Check if user is admin or librarian
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can add books"));
        }
        
        return ResponseEntity.ok(bookService.addBook(book)); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        // Check if user is admin or librarian
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can update books"));
        }
        
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Book not found"));
        }
        
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setGenre(bookDetails.getGenre());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setAvailable(bookDetails.isAvailable());
        
        return ResponseEntity.ok(bookService.addBook(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) { 
        // Check if user is admin or librarian
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrLibrarian = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));
                
        if (!isAdminOrLibrarian) {
            return ResponseEntity.status(403).body(Map.of("error", "Only librarians and admins can delete books"));
        }
        
        bookService.deleteBook(id);
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully")); 
    }
} 