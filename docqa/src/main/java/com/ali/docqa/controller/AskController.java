package com.ali.docqa.controller;

import com.ali.docqa.dto.AskRequest;
import com.ali.docqa.dto.AskResponse;
import com.ali.docqa.service.AskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The RAG query endpoint.
 *
 *   POST /ask  { "question": "..." }  ->  { "answer": "...", "citations": [...] }
 *
 * Requires authentication (SecurityConfig: anyRequest authenticated) — send the Bearer token.
 */
@RestController
public class AskController {

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return askService.ask(request.question());
    }
}
