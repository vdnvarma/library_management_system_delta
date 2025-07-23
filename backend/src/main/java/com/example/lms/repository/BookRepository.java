package com.example.lms.repository;

import com.example.lms.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByIsbn(String isbn);
    List<Book> findByGenreContainingIgnoreCase(String genre);
} 