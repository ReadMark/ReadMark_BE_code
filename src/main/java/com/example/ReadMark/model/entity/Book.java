package com.example.ReadMark.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "books")
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
    @Column(name = "total_book")
    private Integer totalBook;  // 책의 총 페이지 수
    private LocalDate publishedAt;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserBook> userBooks;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FavoritePage> favoritePages;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
