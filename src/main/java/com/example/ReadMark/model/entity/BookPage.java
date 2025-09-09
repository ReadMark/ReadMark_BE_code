package com.example.ReadMark.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_pages")
@Getter
@Setter
public class BookPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer pageNumber;
    
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] imageData;
    
    @Column(nullable = false)
    private LocalDateTime capturedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private Double confidence;
    
    @Column(length = 100)
    private String deviceInfo;
    
    @Column(length = 20)
    private String language;
    
    private Integer numberCount;
    
    private Double textQuality;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
    }
}
