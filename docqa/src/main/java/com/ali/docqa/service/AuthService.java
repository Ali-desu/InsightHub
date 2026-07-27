package com.ali.docqa.service;

import com.ali.docqa.dto.LoginRequest;
import com.ali.docqa.dto.LoginResponse;
import com.ali.docqa.dto.RegisterRequest;
import com.ali.docqa.dto.RegisterResponse;
import com.ali.docqa.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ali.docqa.model.User;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * ==========================================================================================
     *  YOUR METHOD TO WRITE. Injected fields ready above: userRepository, passwordEncoder.
     *
     *  Steps:
     *   1. Reject if taken:  if userRepository.existsByUsername(...) or existsByEmail(...) -> throw
     *      (add those two derived methods to UserRepository — same skill as findByEmail; they
     *       return boolean.)
     *   2. Hash the password:  String hash = passwordEncoder.encode(request.password());
     *   3. Build a User (username, email, passwordhash = hash) and save it.
     *   4. return new RegisterResponse(savedUser.getId(), savedUser.getUsername());
     *
     *  Delete the throw below once implemented.
     * ==========================================================================================
     */
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already taken");
        }
        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), hash);
        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getUsername());
    }

    /**
     * ==========================================================================================
     *  YOUR METHOD TO WRITE. Injected fields ready above: userRepository, passwordEncoder, jwtService.
     *
     *  Steps:
     *   1. Load the user by username — throw if absent.
     *      (add  Optional<User> findByUsername(String username)  to UserRepository — same skill
     *       as findByEmail.)   e.g. userRepository.findByUsername(request.username()).orElseThrow(...)
     *   2. Verify the password against the stored hash:
     *        if (!passwordEncoder.matches(request.password(), user.getPasswordhash())) -> throw
     *      (NOTE: matches(raw, hash) — never encode() the input and compare strings.)
     *   3. Mint a token:  String token = jwtService.generateToken(user.getUsername());
     *   4. return new LoginResponse(token);
     *
     *  Tip: use the SAME error/message for "no such user" and "wrong password" so you don't leak
     *  which usernames exist. Delete the throw below once implemented.
     * ==========================================================================================
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordhash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token);
    }

}
