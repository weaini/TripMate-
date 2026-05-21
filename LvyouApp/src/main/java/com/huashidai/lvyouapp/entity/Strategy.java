package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略实体类
 */
@Entity
@Table(name = "strategies")
@Data
public class Strategy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 10000)
    private String content;
    
    @Column(nullable = false, length = 200)
    private String destination;
    
    @Column(length = 1000)
    private String summary;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal budget;
    
    private Integer duration; // 天数
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyStatus status = StrategyStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyLevel level = StrategyLevel.BEGINNER;
    
    @Column(length = 200)
    private String coverImage;
    
    private Integer viewCount = 0;
    
    private Integer likeCount = 0;
    
    private Integer commentCount = 0;
    
    private Integer shareCount = 0;
    
    private Integer rewardPoints = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 攻略图片
    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StrategyImage> images;
    
    // 攻略评论
    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StrategyComment> comments;
    
    // 攻略点赞
    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StrategyLike> likes;
    
    public enum StrategyType {
        FOOD,           // 美食攻略
        ATTRACTION,     // 景点攻略
        ACCOMMODATION,  // 住宿攻略
        TRANSPORTATION, // 交通攻略
        SHOPPING,       // 购物攻略
        COMPREHENSIVE   // 综合攻略
    }
    
    public enum StrategyStatus {
        PENDING,        // 待审核
        APPROVED,       // 已通过
        REJECTED,       // 已拒绝
        FEATURED        // 精选
    }
    
    public enum StrategyLevel {
        BEGINNER,       // 新手
        INTERMEDIATE,   // 中级
        ADVANCED,       // 高级
        EXPERT          // 专家
    }
}
