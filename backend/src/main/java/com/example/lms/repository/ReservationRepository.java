package com.example.lms.repository;

import com.example.lms.model.Reservation;
import com.example.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
} 