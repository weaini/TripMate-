package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动实体类
 */
@Entity
@Table(name = "activities")
@Data
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Column(nullable = false, length = 200)
    private String destination;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private LocalDateTime registrationDeadline;
    
    @Column(nullable = false)
    private Integer maxParticipants;
    
    private Integer currentParticipants = 0;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status = ActivityStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel level = ActivityLevel.EASY;
    
    @Column(length = 500)
    private String requirements;
    
    @Column(length = 200)
    private String contactInfo;
    
    @Column(length = 200)
    private String coverImage;
    
    private Integer viewCount = 0;
    
    private Integer likeCount = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 活动参与者
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActivityParticipant> participants;
    
    // 活动图片
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActivityImage> images;
    
    // 活动评论
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActivityComment> comments;
    
    public enum ActivityType {
        HIKING,         // 徒步
        PHOTOGRAPHY,    // 摄影采风
        CAMPING,        // 露营
        CULTURAL,       // 文化体验
        ADVENTURE,      // 探险
        RELAXATION,     // 休闲
        OTHER           // 其他
    }
    
    public enum ActivityStatus {
        PENDING,        // 待审核
        APPROVED,       // 已通过
        REJECTED,       // 已拒绝
        ONGOING,        // 进行中
        COMPLETED,      // 已完成
        CANCELLED       // 已取消
    }
    
    public enum ActivityLevel {
        EASY,           // 简单
        MEDIUM,         // 中等
        HARD,           // 困难
        EXPERT          // 专家级
    }
}
