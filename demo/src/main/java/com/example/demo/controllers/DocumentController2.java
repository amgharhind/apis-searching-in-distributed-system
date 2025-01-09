package com.example.demo.controllers;

import com.example.demo.services.DocumentService2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents_V2")
public class DocumentController2 {

    private final DocumentService2 documentService;

    public DocumentController2(DocumentService2 documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Uploaded file is empty or missing."));
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            String documentId = documentService.indexDocument(tempFile, null);
            return ResponseEntity.ok(Map.of("status", "success", "documentId", documentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to index document", "message", e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }
        }
    }

    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadDocuments(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No files were provided."));
        }

        List<String> documentIds = new ArrayList<>();
        for (MultipartFile file : files) {
            File tempFile = null;
            try {
                tempFile = File.createTempFile("upload-", file.getOriginalFilename());
                file.transferTo(tempFile);

                String documentId = documentService.indexDocument(tempFile, null);
                documentIds.add(documentId);
            } catch (IOException e) {
                System.err.println("Failed to index file: " + file.getOriginalFilename() + " - " + e.getMessage());
            } finally {
                if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                    System.err.println("Failed to delete temporary file: " + (tempFile != null ? tempFile.getAbsolutePath() : ""));
                }
            }
        }

        return ResponseEntity.ok(Map.of("status", "success", "documentIds", documentIds));
    }

    @PostMapping(value = "/upload-from-url")
    public ResponseEntity<?> uploadDocumentFromUrl(@RequestParam("url") String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL must not be empty."));
        }

        try {
            new URL(fileUrl).toURI(); // Validate URL
            File tempFile = File.createTempFile("download-", ".tmp");
            try (InputStream in = new URL(fileUrl).openStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("File downloaded successfully: " + tempFile.getAbsolutePath());

            String documentId = documentService.indexDocument(tempFile, fileUrl);

            if (!tempFile.delete()) {
                System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }

            return ResponseEntity.ok(Map.of("status", "success", "documentId", documentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload from URL", "message", e.getMessage()));
        }
    }
}
