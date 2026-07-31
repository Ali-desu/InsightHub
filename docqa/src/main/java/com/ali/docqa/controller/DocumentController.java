package com.ali.docqa.controller;

import com.ali.docqa.dto.ConfirmUploadResponse;
import com.ali.docqa.dto.CreateUploadRequest;
import com.ali.docqa.dto.CreateUploadResponse;
import com.ali.docqa.dto.DocumentSummary;
import com.ali.docqa.model.User;
import com.ali.docqa.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin HTTP layer. Translates requests into service calls and returns DTOs. No business logic here.
 *
 *   POST /documents            -> reserve a slot + get a presigned upload URL (request 1)
 *   POST /documents/{id}/confirm -> mark the upload complete (request 2)
 *
 * The file bytes never pass through this controller — the client PUTs them straight to S3.
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public CreateUploadResponse createUpload(@Valid @RequestBody CreateUploadRequest request,
                                             @AuthenticationPrincipal User user) {
        // 'user' is the User your JwtAuthenticationFilter put in the SecurityContext.
        return documentService.createUpload(request.filename(), request.contentType(), user);
    }

    @GetMapping
    public List<DocumentSummary> list(@AuthenticationPrincipal User user) {
        return documentService.listDocuments(user);
    }

    @PostMapping("/{id}/confirm")
    public ConfirmUploadResponse confirmUpload(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        return documentService.confirmUpload(id, user);
    }
}
