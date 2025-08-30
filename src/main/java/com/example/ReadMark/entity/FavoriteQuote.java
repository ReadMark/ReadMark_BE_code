package com.example.ReadMark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Favorite_Quote")
@Getter
@Setter
public class FavoriteQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favQuoteId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private Integer pageNumber;
    private String content;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

}
