package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Entity
@Table(name = "reports")
@Data
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // 举报人
    
    @Column(nullable = false, length = 200)
    private String targetType; // 举报目标类型：POST, ACTIVITY, STRATEGY, USER
    
    @Column(nullable = false)
    private Long targetId; // 举报目标ID
    
    @Column(nullable = false, length = 1000)
    private String reason;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler; // 处理人
    
    @Column(length = 1000)
    private String handleResult;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime handledAt;
    
    public enum ReportStatus {
        PENDING,        // 待处理
        PROCESSING,     // 处理中
        RESOLVED,       // 已解决
        REJECTED        // 已拒绝
    }
}
