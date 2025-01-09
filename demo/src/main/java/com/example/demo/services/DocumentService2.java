package com.example.demo.services;

import com.example.demo.entities.Document;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Service
public class DocumentService2 {
    private final RestHighLevelClient restHighLevelClient;
    private final Tika tika;

    public DocumentService2(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.tika = new Tika();
    }

    /**
     * Index a single document into Elasticsearch.
     */
    public String indexDocument(File file, String fileUrl) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must not be null and should exist.");
        }

        try {
            // Extract metadata and content
            Document document = extractDocumentMetadata(file, fileUrl);

            // Ensure non-null values for critical fields
            String filename = document.getFilename() != null ? document.getFilename() : "unknown";
            String content = document.getContent() != null ? document.getContent() : "";
            String fileType = document.getFileType() != null ? document.getFileType() : "unknown";
            String filePath = document.getFilePath() != null ? document.getFilePath() : "unknown";
            long fileSize = document.getFileSize() > 0 ? document.getFileSize() : 0L;
            String createdDate = document.getCreatedDate() != null && !document.getCreatedDate().isEmpty()
                    ? document.getCreatedDate()
                    : LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String author = document.getAuthor() != null ? document.getAuthor() : "unknown";

            // Create IndexRequest for Elasticsearch
            IndexRequest request = new IndexRequest("documents")
                    .id(document.getId())
                    .source(
                            Map.of(
                                    "filename", filename,
                                    "content", content,
                                    "fileType", fileType,
                                    "filePath", filePath,
                                    "fileSize", fileSize,
                                    "createdDate", createdDate,
                                    "author", author
                            ),
                            XContentType.JSON
                    );

            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return response.getId();
        } catch (Exception e) {
            throw new IOException("Failed to index document: " + file.getName(), e);
        }
    }

    /**
     * Extract metadata and content using Apache Tika.
     */
    private Document extractDocumentMetadata(File file, String fileUrl) throws IOException, TikaException, SAXException {
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler();
        String httpLastModified = null, httpAuthor = null;

        String originalFilename = file.getName();
        if (fileUrl != null) {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            httpLastModified = connection.getHeaderField("Last-Modified");
            httpAuthor = connection.getHeaderField("Author");
            originalFilename = extractFilename(fileUrl, connection.getHeaderField("Content-Disposition"));
        }

        // Extract content and metadata
        try (FileInputStream inputStream = new FileInputStream(file)) {
            new AutoDetectParser().parse(inputStream, handler, metadata);
        }

        String creationDate = getMetadataValue(metadata, "Creation-Date", httpLastModified);
        String author = getMetadataValue(metadata, "Author", httpAuthor);

        return new Document(UUID.randomUUID().toString(), originalFilename, handler.toString(),
                getFileExtension(file), file.getAbsolutePath(), file.length(), creationDate, author);
    }

    private String getMetadataValue(Metadata metadata, String primaryKey, String fallbackValue) {
        String value = metadata.get(primaryKey);
        return value != null ? value : fallbackValue;
    }

    private String extractFilename(String fileUrl, String contentDisposition) {
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            return contentDisposition.split("filename=")[1].replace("\"", "").trim();
        }
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);
    }
}
