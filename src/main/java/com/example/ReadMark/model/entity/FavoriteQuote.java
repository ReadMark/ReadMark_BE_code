package com.example.ReadMark.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_quotes")
@Getter
@Setter
public class FavoriteQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favQuoteId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer pageNumber;
    private String content;
    @Column(length = 255)
    private String bookTitle;      // 책 제목
    
    @Column(length = 1000)
    private String coverImageUrl;  // 개별 표지 이미지 URL
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

}
