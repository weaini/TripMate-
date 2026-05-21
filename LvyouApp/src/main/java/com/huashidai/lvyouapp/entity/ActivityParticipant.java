package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 活动参与者实体类
 */
@Entity
@Table(name = "activity_participants")
@Data
public class ActivityParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.PENDING;
    
    @Column(length = 500)
    private String applicationNote;
    
    @Column(length = 500)
    private String rejectionReason;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime appliedAt;
    
    private LocalDateTime approvedAt;
    
    /**
     * 参与者状态枚举
     */
    public enum ParticipantStatus {
        PENDING,    // 待审核
        APPROVED,   // 已通过
        REJECTED,   // 已拒绝
        CANCELLED   // 已取消
    }
}