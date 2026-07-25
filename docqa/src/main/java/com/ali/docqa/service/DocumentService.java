package com.ali.docqa.service;

import com.ali.docqa.dto.ConfirmUploadResponse;
import com.ali.docqa.dto.CreateUploadResponse;
import com.ali.docqa.repository.DocumentRepository;
import com.ali.docqa.repository.UserRepository;
import com.ali.docqa.model.Document;
import com.ali.docqa.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.time.Duration;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public DocumentService(DocumentRepository documentRepository,
                           UserRepository userRepository,
                           S3Presigner s3Presigner,
                           @Value("${aws.s3.bucket}") String bucket) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    /**
     * ==========================================================================================
     *  THIS METHOD IS YOURS TO WRITE — it's the one with the real learning value.
     *  The fields you need are already injected above: documentRepository, userRepository,
     *  s3Presigner, bucket.
     *
     *  Steps:
     *   1. Load the owner: userRepository.findById(userId) — throw if absent.
     *   2. Build a unique S3 key string, e.g.  userId + "/" + UUID.randomUUID() + "/" + filename.
     *   3. Create a Document (status PENDING, s3key = the key, filename, mimetype = contentType,
     *      user = owner) and save it via documentRepository.
     *   4. Presign a PUT URL for that key using the AWS SDK builders:
     *        - PutObjectRequest        (bucket, key, contentType)
     *        - PutObjectPresignRequest (signatureDuration = Duration.ofMinutes(15), the PutObjectRequest)
     *        - s3Presigner.presignPutObject(...).url().toString()
     *   5. return new CreateUploadResponse(savedDocument.getId(), presignedUrl);
     *
     *  Delete the throw below once you've implemented it.
     * ==========================================================================================
     */
    public CreateUploadResponse createUpload(String filename, String contentType, Long userId) {
        User owner = this.userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String s3Key = userId + "/" + UUID.randomUUID() + "/" + filename;
        Document document = new Document();
        document.setS3key(s3Key);
        document.setFilename(filename);
        document.setMimetype(contentType);
        document.setStatus(Document.DocumentStatus.PENDING);
        document.setUser(owner);
        Document savedDocument = this.documentRepository.save(document);
        String presignedUrl = this.s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(
                                PutObjectRequest.builder()
                                        .bucket(this.bucket)
                                        .key(s3Key)
                                        .contentType(contentType)
                                        .build()
                        )
                        .build()
        ).url().toString();
        return new CreateUploadResponse(savedDocument.getId(), presignedUrl);

    }

    /**
     * Left as a stub for a later session. When you get here:
     *   1. Load the Document by id (throw if absent).
     *   2. (optional) HEAD the object in S3 to verify the bytes really arrived.
     *   3. Set status = UPLOADED, save.
     *   4. return new ConfirmUploadResponse(doc.getId(), doc.getStatus().name());
     */
    public ConfirmUploadResponse confirmUpload(Long documentId) {
        Document document = this.documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        document.setStatus(Document.DocumentStatus.UPLOADED);
        this.documentRepository.save(document);
        return new ConfirmUploadResponse(document.getId(), document.getStatus().name());
    }
}
