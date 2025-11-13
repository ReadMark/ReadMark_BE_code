package com.example.ReadMark.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_failures")
@Getter
@Setter
public class MissionFailure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long failureId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
    
    @Column(nullable = false)
    private LocalDate failureDate;
    
    @Column(nullable = false)
    private LocalDateTime failedAt;
    
    @Column(columnDefinition = "TEXT")
    private String failureReason; // 실패 사유 (선택사항)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
