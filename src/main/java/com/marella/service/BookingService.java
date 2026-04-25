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

        // 🚨 ROOM LIMIT CHECK (5 rooms per type)
        int bookedCount = bookingRepository.countByRoomNameAndStatusNot(
                booking.getRoomName(),
                "CHECKED_OUT"
        );

        if (bookedCount >= 5) {
            throw new RuntimeException("All rooms of this type are occupied ❌");
        }

        // 🚨 PAYMENT LOGIC
        if ("Card".equalsIgnoreCase(booking.getPaymentMethod())) {

            if (booking.getPaymentId() == null) {
                throw new RuntimeException("Payment not completed. Please try again.");
            }

            booking.setPaymentStatus("SUCCESS");

        } else {
            // Pay at hotel
            booking.setPaymentStatus("PENDING");
        }

        // 🚨 DEFAULT STATUS
        booking.setStatus("CHECKED_IN");

        // 🚨 SAVE BOOKING
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