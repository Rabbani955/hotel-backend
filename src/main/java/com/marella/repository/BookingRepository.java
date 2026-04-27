package com.marella.repository;

import com.marella.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ✅ Get active bookings for a room type
    List<Booking> findByRoomNameAndStatusNot(String roomName, String status);

    // ✅ Count overlapping rooms (DATE-WISE LOGIC 🔥)
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN b.roomsCount IS NULL OR b.roomsCount = 0 THEN 1 
                ELSE b.roomsCount 
            END
        ), 0)
        FROM Booking b
        WHERE b.roomName = :roomName
        AND b.status = 'CHECKED_IN'
        AND b.checkIn <= :checkOut
        AND b.checkOut >= :checkIn
    """)
    int countOverlappingRooms(
        @Param("roomName") String roomName,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    // ✅ User limit (DATE-WISE 🔥)
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN b.roomsCount IS NULL OR b.roomsCount = 0 THEN 1 
                ELSE b.roomsCount 
            END
        ), 0)
        FROM Booking b
        WHERE b.email = :email
        AND b.status = 'CHECKED_IN'
        AND b.checkIn <= :checkOut
        AND b.checkOut >= :checkIn
    """)
    int countUserBookedRoomsForDates(
        @Param("email") String email,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}