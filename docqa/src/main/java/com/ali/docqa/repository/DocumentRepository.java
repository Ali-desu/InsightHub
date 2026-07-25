package com.ali.docqa.repository;

import org.springframework.stereotype.Repository;
import com.ali.docqa.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByS3key(String s3key);
}

