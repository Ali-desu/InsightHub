package com.ali.docqa.dto;

/**
 * Response after a successful login: the signed JWT the client attaches to future requests
 * as "Authorization: Bearer <token>".
 */
public record LoginResponse(String token) {
}
