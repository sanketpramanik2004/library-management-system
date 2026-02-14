package com.library.library_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ✅ Enable CORS
                .cors(cors -> {
                })

                // ✅ Stateless JWT authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ================= AUTH =================
                        .requestMatchers("/auth/**").permitAll()

                        // ================= ADMIN ONLY =================
                        .requestMatchers("/books/add").hasRole("ADMIN")
                        .requestMatchers("/books/delete/**").hasRole("ADMIN")

                        // ⭐ Admin monitoring dashboard
                        .requestMatchers("/transactions/all").hasRole("ADMIN")

                        // ================= USER + ADMIN =================
                        .requestMatchers("/books/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/transactions/stats").hasRole("ADMIN")
                        .requestMatchers("/transactions/**").hasAnyRole("USER", "ADMIN")

                        // ================= DEFAULT =================
                        .anyRequest().authenticated())

                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        // ✅ Attach JWT Filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================= PASSWORD ENCODER =================
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= CORS CONFIG =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500"));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
