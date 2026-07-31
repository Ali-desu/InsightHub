package com.ali.docqa.controller;

import com.ali.docqa.dto.AskRequest;
import com.ali.docqa.dto.AskResponse;
import com.ali.docqa.model.User;
import com.ali.docqa.service.AskService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The RAG query endpoint.
 *
 *   POST /ask  { "question": "...", "documentIds": [1,2] }  ->  { "answer": "...", "citations": [...] }
 *
 * Requires authentication. Retrieval is scoped to the authenticated user's own documents.
 */
@RestController
public class AskController {

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request,
                           @AuthenticationPrincipal User user) {
        return askService.ask(request.question(), user, request.documentIds());
    }
}
