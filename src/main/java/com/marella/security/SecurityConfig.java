package com.marella.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ✅ CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ❌ Disable CSRF
            .csrf(csrf -> csrf.disable())

            // ❌ Stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ❌ Disable default login
            .httpBasic(h -> h.disable())
            .formLogin(f -> f.disable())

            // ✅ Authorization
            .authorizeHttpRequests(auth -> auth

                // 🔥 VERY IMPORTANT (Preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ======================
                // ✅ PUBLIC APIs
                // ======================
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/occupied-rooms").permitAll()

                // 🔥🔥🔥 FIX (PAYMENT API)
                .requestMatchers("/api/payment/**").permitAll()

                // ======================
                // 🔒 ADMIN APIs
                // ======================
                .requestMatchers("/api/bookings/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/checkout/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasRole("ADMIN")
                .requestMatchers("/api/rooms/admin/**").hasRole("ADMIN")

                // ======================
                // 🔐 ALL OTHERS
                // ======================
                .anyRequest().authenticated()
            )

            // ✅ JWT FILTER
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================
    // ✅ CORS CONFIG
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://hotel-marella-royal-inn.onrender.com",
            "https://*.onrender.com",
            "https://*.vercel.app"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}