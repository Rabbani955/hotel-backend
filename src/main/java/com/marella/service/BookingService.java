package com.marella.service;

import com.marella.model.Booking;
import com.marella.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public Booking createBooking(Booking booking) {

        int requestedRooms = Math.max(1, booking.getRoomsCount());

        // 🔥 DATE-WISE ROOM AVAILABILITY
        int overlappingRooms = bookingRepository.countOverlappingRooms(
                booking.getRoomName(),
                booking.getCheckIn(),
                booking.getCheckOut()
        );

        if (overlappingRooms + requestedRooms > 5) {

            int available = 5 - overlappingRooms;

            if (available <= 0) {
                throw new RuntimeException("No rooms available for selected dates ❌");
            } else {
                throw new RuntimeException("Only " + available + " rooms available for selected dates ❌");
            }
        }

        // 🔥 USER LIMIT (DATE-WISE)
        int userRooms = bookingRepository.countUserBookedRoomsForDates(
                booking.getEmail(),
                booking.getCheckIn(),
                booking.getCheckOut()
        );

        if (userRooms + requestedRooms > 5) {
            throw new RuntimeException("You can book maximum 5 rooms for selected dates ❌");
        }

        // 💳 PAYMENT LOGIC
        if ("Card".equalsIgnoreCase(booking.getPaymentMethod())) {

            if (booking.getPaymentId() == null) {
                throw new RuntimeException("Payment not completed ❌");
            }

            booking.setPaymentStatus("SUCCESS");

        } else {
            booking.setPaymentStatus("PENDING");
        }

        booking.setStatus("CHECKED_IN");

        return bookingRepository.save(booking);
    }

    // ✅ CHECKOUT
    public Booking checkout(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CHECKED_OUT");

        return bookingRepository.save(booking);
    }
}