package com.example.demo.entities;

import lombok.Data;

import java.util.Objects;

@Data
public class Document {
    private String id;
    private String filename;
    private String content;
    private String fileType; // e.g., txt, pdf
    private String filePath;
    private long fileSize; // File size in bytes
    private String createdDate; // Metadata
    private String author; // Metadata

    // Scoring fields
    private double luceneScore; // Original score from Elasticsearch
    private double finalScore; // Re-ranked score


    // Constructor with parameters
    public Document(String id, String filename, String content, String fileType, String filePath, long fileSize, String createdDate, String author) {
        this.id = id;
        this.filename = filename;
        this.content = content;
        this.fileType = fileType;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.createdDate = createdDate;
        this.author = author;

        // Default scoring fields to 0
        this.luceneScore = 0.0;
        this.finalScore = 0.0;
    }

    public Document() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return fileSize == document.fileSize &&
                Double.compare(document.luceneScore, luceneScore) == 0 &&
                Double.compare(document.finalScore, finalScore) == 0 &&
                Objects.equals(id, document.id) &&
                Objects.equals(filename, document.filename) &&
                Objects.equals(content, document.content) &&
                Objects.equals(fileType, document.fileType) &&
                Objects.equals(filePath, document.filePath) &&
                Objects.equals(createdDate, document.createdDate) &&
                Objects.equals(author, document.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename, content, fileType, filePath, fileSize, createdDate, author, luceneScore, finalScore);
    }
    private String explanation;


}
