package com.ali.docqa.config;

import com.ali.docqa.repository.UserRepository;
import com.ali.docqa.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;

/**
 * Runs once per request. If the request carries a valid "Authorization: Bearer <jwt>" header,
 * it puts the authenticated user into Spring Security's SecurityContext. It does NOT reject
 * anything — the authorizeHttpRequests rules do that by checking whether a user is in the context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No bearer token -> nothing to authenticate. Let the request continue; if the endpoint
        // requires auth, the authorization rules will block it downstream.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer "

        if(jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtService.extractUsername(token);
            userRepository.findByUsername(username).ifPresent(user -> {
                var authentication = new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        // Always continue the chain.
        filterChain.doFilter(request, response);
    }
}
