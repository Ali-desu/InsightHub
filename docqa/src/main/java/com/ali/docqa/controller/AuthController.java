package com.ali.docqa.controller;

import com.ali.docqa.dto.LoginRequest;
import com.ali.docqa.dto.LoginResponse;
import com.ali.docqa.dto.RegisterRequest;
import com.ali.docqa.dto.RegisterResponse;
import com.ali.docqa.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public auth endpoints. Permitted without a token by SecurityConfig (/auth/**).
 *
 *   POST /auth/register -> create an account
 *   POST /auth/login    -> (next sitting) verify credentials + issue a JWT
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
