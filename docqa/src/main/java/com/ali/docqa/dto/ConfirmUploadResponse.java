package com.ali.docqa.dto;

/**
 * Response for confirming an upload: the document's id and its (now updated) status.
 */
public record ConfirmUploadResponse(Long documentId, String status) {
}
