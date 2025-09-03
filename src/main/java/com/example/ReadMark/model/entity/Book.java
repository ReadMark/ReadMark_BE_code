package com.example.ReadMark.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "Book")
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    private String author;
    private String publisher;
    private String coverImageUrl;
    private LocalDate publishedAt;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<UserBook> userBooks;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<FavoritePage> favoritePages;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<FavoriteQuote> favoriteQuotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
