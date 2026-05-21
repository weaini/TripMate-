package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 达人申请实体类
 */
@Entity
@Table(name = "expert_applications")
@Data
public class ExpertApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 1000)
    private String reason;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 200)
    private String expertise;
    
    @Column(length = 200)
    private String contactInfo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(length = 500)
    private String adminNote;
    
    @Column(length = 500)
    private String rejectReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum ApplicationStatus {
        PENDING,        // 待审核
        APPROVED,       // 已通过
        REJECTED        // 已拒绝
    }
}
