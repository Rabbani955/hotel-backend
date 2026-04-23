package com.marella.controller;

import com.marella.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;



@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ ADD HERE
    @Value("${admin.username}")
    private String adminUser;

    @Value("${admin.password}")
    private String adminPass;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> data) {

        String username = data.get("username");
        String password = data.get("password");

        // ✅ UPDATED LINE
        if (adminUser.equals(username) && adminPass.equals(password)) {

            String role = "ADMIN";

            String token = jwtUtil.generateToken(username, role);

            return Map.of(
                "token", token,
                "type", "Bearer",
                "role", "ADMIN"
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}