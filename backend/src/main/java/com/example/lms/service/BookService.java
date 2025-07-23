package com.example.lms.service;

import com.example.lms.model.Book;
import com.example.lms.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepo;
    public BookService(BookRepository bookRepo) { this.bookRepo = bookRepo; }

    public List<Book> searchByTitle(String title) { return bookRepo.findByTitleContainingIgnoreCase(title); }
    public List<Book> searchByAuthor(String author) { return bookRepo.findByAuthorContainingIgnoreCase(author); }
    public List<Book> searchByIsbn(String isbn) { return bookRepo.findByIsbn(isbn); }
    public List<Book> searchByGenre(String genre) { return bookRepo.findByGenreContainingIgnoreCase(genre); }
    public Book addBook(Book book) { return bookRepo.save(book); }
    public void deleteBook(Long id) { bookRepo.deleteById(id); }
    public List<Book> getAllBooks() { return bookRepo.findAll(); }
    public Optional<Book> getBookById(Long id) { return bookRepo.findById(id); }
} 