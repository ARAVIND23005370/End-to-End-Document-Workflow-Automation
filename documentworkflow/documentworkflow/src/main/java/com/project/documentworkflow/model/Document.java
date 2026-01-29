package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String documentType;
    private String filePath;
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // getters & setters
}
