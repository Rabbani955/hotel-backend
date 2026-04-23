package com.marella.repository;

import com.marella.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ✅ Add this method
    List<Room> findBySoldOutFalse();

}