package com.marella.controller;

import com.marella.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ Admin credentials (can come from application.properties)
    @Value("${admin.username:Rabbani}")
    private String adminUser;

    @Value("${admin.password:Rabbani123}")
    private String adminPass;

    // =====================================================
    // ✅ LOGIN API
    // =====================================================
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> data) {

        String username = data.get("username");
        String password = data.get("password");

        // ✅ Null safety + trim
        if (username == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing credentials");
        }

        username = username.trim();
        password = password.trim();

        // ✅ Validate admin credentials
        if (adminUser.equals(username) && adminPass.equals(password)) {

            // 🔥 FORCE CORRECT ROLE (IMPORTANT)
            String role = "ADMIN";

            // 🔥 Generate token with role
            String token = jwtUtil.generateToken(username, role);

            // ✅ Response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("role", role);

            return response;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}