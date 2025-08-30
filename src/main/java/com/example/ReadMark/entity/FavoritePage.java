package com.example.ReadMark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Favorite_Page")
@Getter
@Setter
public class FavoritePage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favPageId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private int pageNumber;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

}
