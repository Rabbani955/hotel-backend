package com.marella.repository;

import com.marella.model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityRepository
        extends JpaRepository<RoomAvailability, Long> {

    @Query("""
        SELECT r
        FROM RoomAvailability r
        WHERE r.roomId = :roomId
        AND r.soldOut = true
        AND r.startDate <= :checkOut
        AND r.endDate >= :checkIn
    """)
    List<RoomAvailability> findBlockedDates(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}