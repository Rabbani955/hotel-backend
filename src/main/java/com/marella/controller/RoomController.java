package com.marella.controller;

import com.marella.model.Room;
import com.marella.repository.RoomRepository;
import com.marella.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final JwtUtil jwtUtil;

    public RoomController(RoomRepository roomRepository, JwtUtil jwtUtil) {
        this.roomRepository = roomRepository;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Guest API
    @GetMapping
    public List<Room> getRooms() {
        return roomRepository.findBySoldOutFalse();
    }

    // ✅ Admin: Get all rooms
    @GetMapping("/admin")
    public List<Room> getAllRoomsForAdmin(HttpServletRequest request) {
        validateAdmin(request);
        return roomRepository.findAll();
    }

    // ✅ Admin: Update sold out status
    @PutMapping("/admin/soldout/{id}")
    public Room markSoldOut(@PathVariable Long id,
                            @RequestParam boolean status,
                            HttpServletRequest request) {

        validateAdmin(request);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setSoldOut(status);

        return roomRepository.save(room);
    }

    // ✅ JWT validation (common method)
    private void validateAdmin(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid token");
        }

        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Unauthorized");
        }
    }
}