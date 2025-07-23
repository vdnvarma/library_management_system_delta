package com.example.lms.service;

import com.example.lms.model.*;
import com.example.lms.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepo;
    
    public ReservationService(ReservationRepository reservationRepo) { 
        this.reservationRepo = reservationRepo; 
    }

    public Reservation reserveBook(Book book, User user) {
        Reservation r = new Reservation();
        r.setBook(book);
        r.setUser(user);
        r.setReservationDate(LocalDate.now());
        r.setActive(true);
        r.setNotified(false);
        return reservationRepo.save(r);
    }
    
    public List<Reservation> getUserReservations(User user) {
        return reservationRepo.findByUser(user);
    }
    
    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }
    
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepo.findById(id);
    }
    
    public void cancelReservation(Long id) {
        reservationRepo.findById(id).ifPresent(reservation -> {
            reservation.setActive(false);
            reservationRepo.save(reservation);
        });
    }
    
    public Reservation save(Reservation r) { 
        return reservationRepo.save(r); 
    }
} 