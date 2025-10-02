package com.example.ReadMark.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 500)
    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 관계 설정 - LazyInitializationException 방지를 위해 제거
    // 필요시 별도 API로 관계 데이터 조회
    
    // 임시로 userBooks 필드를 추가하되 @JsonIgnore로 JSON 직렬화에서 제외
    @Transient
    @JsonIgnore
    private Object userBooks;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
