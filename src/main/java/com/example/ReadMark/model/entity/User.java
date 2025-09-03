package com.example.ReadMark.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 관계 설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserBook> userBooks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReadingLog> readingLogs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FavoritePage> favoritePages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FavoriteQuote> favoriteQuotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
