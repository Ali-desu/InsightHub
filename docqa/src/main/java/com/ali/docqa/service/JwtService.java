package com.ali.docqa.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Creates and verifies JWTs (using the JJWT library). Infrastructure — you consume this from
 * AuthService (to mint a token on login) and from the JWT filter (to validate incoming tokens).
 *
 * How it works:
 *  - The server holds a SECRET key (from config). It signs every token with it.
 *  - generateToken(...) builds header + claims (subject = username, issued/expiry) and signs.
 *  - Verifying a token recomputes the signature with the same secret; if it matches and the
 *    token isn't expired, it's trusted. No server-side session is stored.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        // HMAC-SHA256 needs a key of at least 256 bits (32 bytes) — hence a long secret string.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Mint a signed token whose "subject" is the username. */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /** Read the username (subject) out of a token — throws if the signature is invalid. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** True if the token's signature is valid AND it hasn't expired. */
    public boolean isTokenValid(String token) {
        try {
            return parseClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** Verify the signature with our secret and return the claims (payload). */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
