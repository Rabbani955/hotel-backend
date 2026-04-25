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
            // ✅ Enable CORS using our config
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ❌ Disable CSRF (required for APIs)
            .csrf(csrf -> csrf.disable())

            // ❌ No session (JWT based)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ❌ Disable default login
            .httpBasic(h -> h.disable())
            .formLogin(f -> f.disable())

            // ✅ Authorization rules
            /*.authorizeHttpRequests(auth -> auth

                // 🔥 VERY IMPORTANT (Fix preflight issue)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public APIs
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/bookings/**").permitAll()
                .requestMatchers("/api/rooms/**").permitAll()
                .requestMatchers("/api/payment/**").permitAll()

                // Admin only
                // ADMIN ONLY
                .requestMatchers("/api/bookings/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/checkout/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasRole("ADMIN")

                // Everything else secured
                .anyRequest().authenticated()
            ) */
            
            .authorizeHttpRequests(auth -> auth

            	    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            	    // PUBLIC
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/api/bookings/occupied-rooms").permitAll()

            	    // ADMIN
            	    .requestMatchers("/api/bookings/admin").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.PUT, "/api/bookings/checkout/**").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasRole("ADMIN")
            	    .requestMatchers("/api/rooms/admin/**").hasRole("ADMIN")

            	    .anyRequest().authenticated()
            	)

            // ✅ JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================
    // ✅ CORS CONFIG (FINAL FIX)
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 🔥 REQUIRED
        config.setAllowCredentials(true);

        // 🔥 IMPORTANT: match EXACT frontend URL
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://hotel-marella-royal-inn.onrender.com" // 🔥 YOUR FRONTEND DEPLOY URL (update later)
        ));

        // ✅ Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // ✅ Allow all methods
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 🔥 IMPORTANT (for JWT Authorization header)
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}