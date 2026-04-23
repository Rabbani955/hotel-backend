package com.marella.service;

import com.marella.model.Booking;
import com.marella.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    // ✅ CREATE BOOKING (FINAL VERSION - MULTI ROOM SUPPORT)
    public Booking createBooking(Booking booking) {

        // 🚨 1. Prevent overlapping booking (date-wise safety)
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                booking.getRoomName(),
                booking.getCheckIn(),
                booking.getCheckOut()
        );

        if (isOverlapping) {
            throw new RuntimeException("Room already booked for selected dates!");
        }

        // 🚨 2. ROOM LIMIT CHECK (5 rooms per type)
        int bookedCount = bookingRepository.countByRoomNameAndStatusNot(
                booking.getRoomName(),
                "CHECKED_OUT"
        );

        if (bookedCount >= 5) {
            throw new RuntimeException("All rooms of this type are occupied ❌");
        }

        // 🚨 3. PAYMENT LOGIC
        if ("Card".equalsIgnoreCase(booking.getPaymentMethod())) {

            if (booking.getPaymentId() == null) {
                throw new RuntimeException("Payment not completed. Please try again.");
            }

            booking.setPaymentStatus("SUCCESS");

        } else {
            // Pay at hotel
            booking.setPaymentStatus("PENDING");
        }

        // 🚨 4. SET DEFAULT STATUS (VERY IMPORTANT)
        booking.setStatus("CHECKED_IN");

        // 🚨 5. SAVE BOOKING
        return bookingRepository.save(booking);
    }

    // ✅ CHECKOUT (FREE ROOM)
    public Booking checkout(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CHECKED_OUT");

        return bookingRepository.save(booking);
    }
}