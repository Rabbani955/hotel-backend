package com.marella.controller;

import com.marella.model.Booking;
import com.marella.service.BookingService;
import com.marella.repository.BookingRepository;
import com.marella.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final JwtUtil jwtUtil;

    public BookingController(BookingService bookingService,
                             BookingRepository bookingRepository,
                             JwtUtil jwtUtil) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.jwtUtil = jwtUtil;
    }

    // ✅ CREATE BOOKING
    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {

        if (booking.getCheckIn() == null || booking.getCheckOut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in & Check-out required");
        }

        if (booking.getCheckOut().isBefore(booking.getCheckIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }

        if (booking.getPaymentMethod() == null || booking.getPaymentMethod().isEmpty()) {
            booking.setPaymentMethod("Pay at Hotel");
        }

        try {
            return bookingService.createBooking(booking);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    // ✅ ADMIN BOOKINGS
    @GetMapping("/admin")
    public List<Booking> getAllBookings(HttpServletRequest request) {
        validateAdmin(request);
        return bookingRepository.findAll();
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id, HttpServletRequest request) {

        validateAdmin(request);

        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        bookingRepository.deleteById(id);
    }

    // ✅ CHECKOUT
    @PutMapping("/checkout/{id}")
    public Booking checkout(@PathVariable Long id, HttpServletRequest request) {
        validateAdmin(request);
        return bookingService.checkout(id);
    }

    // ✅ OCCUPIED ROOMS (for frontend)
    @GetMapping("/occupied-rooms")
    public List<Map<String, Object>> getOccupiedRooms() {

        return bookingRepository.findAll()
                .stream()
                .filter(b -> !"CHECKED_OUT".equals(b.getStatus()))
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomName", b.getRoomName());
                    map.put("roomsCount", b.getRoomsCount());
                    return map;
                })
                .toList();
    }

    // 🔐 ADMIN AUTH
    private void validateAdmin(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }
}