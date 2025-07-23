package com.example.lms.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Book book;
    @ManyToOne
    private User user;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Double finePaid;
} 