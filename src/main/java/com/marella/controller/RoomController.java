package com.marella.controller;

import com.marella.model.Room;
import com.marella.model.RoomAvailability;
import com.marella.repository.BookingRepository;
import com.marella.repository.RoomAvailabilityRepository;
import com.marella.repository.RoomRepository;
import com.marella.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final BookingRepository bookingRepository;
    private final JwtUtil jwtUtil;

    public RoomController(
            RoomRepository roomRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            BookingRepository bookingRepository,
            JwtUtil jwtUtil) {

        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.bookingRepository = bookingRepository;
        this.jwtUtil = jwtUtil;
    }

    // ==================================================
    // GUEST APIs
    // ==================================================

    @GetMapping
    public List<Room> getRooms() {
        return roomRepository.findAll();
    }

    // ==================================================
    // DATE-WISE AVAILABLE ROOMS
    // ==================================================

    @GetMapping("/available")
    public List<Room> getAvailableRooms(
            @RequestParam String checkIn,
            @RequestParam String checkOut) {

        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);

        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .filter(room -> {

                    // Global sold out
                    if (room.isSoldOut()) {
                        return false;
                    }

                    // Date-wise sold out by admin
                    boolean blocked =
                            !roomAvailabilityRepository.findBlockedDates(
                                    room.getId(),
                                    in,
                                    out
                            ).isEmpty();

                    if (blocked) {
                        return false;
                    }

                    // Already booked rooms for those dates
                    int bookedRooms =
                            bookingRepository.countOverlappingRooms(
                                    room.getName(),
                                    in,
                                    out
                            );

                    return bookedRooms < 5;
                })
                .toList();
    }

    // ==================================================
    // ADMIN APIs
    // ==================================================

    @GetMapping("/admin")
    public List<Room> getAllRoomsForAdmin(HttpServletRequest request) {

        validateAdmin(request);

        return roomRepository.findAll();
    }

    @PutMapping("/admin/soldout/{id}")
    public Room markSoldOut(
            @PathVariable Long id,
            @RequestParam boolean status,
            HttpServletRequest request) {

        validateAdmin(request);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setSoldOut(status);

        return roomRepository.save(room);
    }

    @PutMapping("/admin/update-price/{id}")
    public Room updatePrice(
            @PathVariable Long id,
            @RequestParam int price,
            HttpServletRequest request) {

        validateAdmin(request);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setBasePrice(price);

        return roomRepository.save(room);
    }

    // ==================================================
    // DATE-WISE SOLD OUT
    // ==================================================

    @PostMapping("/admin/date-soldout")
    public RoomAvailability markDateSoldOut(
            @RequestBody RoomAvailability request,
            HttpServletRequest requestHttp) {

        validateAdmin(requestHttp);

        return roomAvailabilityRepository.save(request);
    }

    // ==================================================
    // JWT VALIDATION
    // ==================================================

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