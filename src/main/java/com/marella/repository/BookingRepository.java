package com.marella.repository;

import com.marella.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ✅ Check overlapping bookings
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.roomName = :roomName
        AND b.status = 'CHECKED_IN'
        AND b.checkIn <= :checkOut
        AND b.checkOut >= :checkIn
    """)
    boolean existsOverlappingBooking(
            @Param("roomName") String roomName,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // ✅ Count active bookings (for 5-room logic)
    int countByRoomNameAndStatusNot(String roomName, String status);
}