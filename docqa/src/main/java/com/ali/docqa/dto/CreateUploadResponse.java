package com.ali.docqa.dto;

/**
 * Response for a create-upload request: the new document's id plus the presigned URL the client
 * PUTs the file bytes to (directly to S3).
 */
public record CreateUploadResponse(Long documentId, String uploadUrl) {
}
