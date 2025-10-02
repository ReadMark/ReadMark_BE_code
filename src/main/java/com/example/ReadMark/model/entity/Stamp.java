package com.example.ReadMark.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stamps")
@Getter
@Setter
public class Stamp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stampId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @Column(nullable = false)
    private LocalDate earnedDate;  // 도장을 받은 날짜
    
    @Column(nullable = false)
    private Integer pagesRead;     // 해당 날에 읽은 페이지 수
    
    @Column(nullable = false)
    private LocalDateTime createdAt;  // 도장 획득 시간
    
    @Column(length = 500)
    private String description;    // 도장 획득 설명 (선택사항)
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (earnedDate == null) {
            earnedDate = LocalDate.now();
        }
    }
}
