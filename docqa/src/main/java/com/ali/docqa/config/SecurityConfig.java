package com.ali.docqa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security setup. Replaces the default "lock everything + generated password"
 * behavior that just adding the security starter turns on.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** BCrypt: the salted, deliberately-slow hashing algorithm used for passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Allow the React dev server (different origin) to call this API.
                .cors(Customizer.withDefaults())
                // Stateless token API, not cookie/session based -> CSRF protection doesn't apply.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Register + login MUST be reachable without being logged in.
                        .requestMatchers("/auth/**").permitAll()
                        // Everything else (incl. /documents) now requires a valid JWT.
                        .anyRequest().authenticated()
                )
                // Don't keep server-side sessions; trust the token on each request.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Run our JWT filter before Spring's username/password filter, so a valid token
                // populates the SecurityContext before the authorization rules are evaluated.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** CORS policy: allow the Vite dev server origin to call the API (incl. preflight). */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
