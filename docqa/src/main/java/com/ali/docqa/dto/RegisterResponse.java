package com.ali.docqa.dto;

/**
 * Response after a successful registration. Never echoes the password or its hash.
 */
public record RegisterResponse(Long id, String username) {
}
