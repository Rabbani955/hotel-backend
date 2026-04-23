package com.marella.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

            // ❌ Disable CSRF (for APIs)
            .csrf(csrf -> csrf.disable())

            // ✅ No session (JWT based)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ❌ Disable default login popup
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())

            // 🔐 Authorization rules
            .authorizeHttpRequests(auth -> auth

                // ✅ PUBLIC
                .requestMatchers("/api/auth/**").permitAll()

                // 🔐 ADMIN ONLY
                .requestMatchers("/api/bookings/admin").hasRole("ADMIN")

                // ✅ OTHER PUBLIC APIs
                .requestMatchers(
                    "/api/bookings/**",
                    "/api/rooms/**",
                    "/api/payment/**"
                ).permitAll()

                // 🔒 everything else
                .anyRequest().authenticated()
            )

            // ✅ JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CORS CONFIG (IMPORTANT FOR FRONTEND)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        // 🔥 ADD YOUR FRONTEND URL HERE
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",   // local
            "https://your-frontend-url.com" // deployed frontend (CHANGE THIS)
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}