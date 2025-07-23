package com.example.lms.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Book book;
    @ManyToOne
    private User user;
    private LocalDate reservationDate;
    private boolean active;
    private boolean notified;
} 