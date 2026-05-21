package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略实体类
 */
@Entity
@Table(name = "guides")
@Data
@EqualsAndHashCode(callSuper = false)
public class Guide {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 500)
    private String summary;
    
    @Column(length = 200)
    private String coverImage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuideType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuideStatus status = GuideStatus.PUBLISHED;
    
    @Column(nullable = false)
    private Integer viewCount = 0;
    
    @Column(nullable = false)
    private Integer likeCount = 0;
    
    @Column(nullable = false)
    private Integer commentCount = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuideComment> comments;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 攻略类型枚举
     */
    public enum GuideType {
        TRAVEL_TIPS("旅行贴士"),
        DESTINATION_GUIDE("目的地攻略"),
        TRANSPORTATION("交通指南"),
        ACCOMMODATION("住宿推荐"),
        FOOD_GUIDE("美食攻略"),
        CULTURE_GUIDE("文化体验"),
        BUDGET_TIPS("省钱攻略"),
        SAFETY_TIPS("安全贴士"),
        PHOTOGRAPHY("摄影技巧"),
        OTHER("其他");
        
        private final String displayName;
        
        GuideType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 攻略状态枚举
     */
    public enum GuideStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        HIDDEN("隐藏"),
        DELETED("已删除");
        
        private final String displayName;
        
        GuideStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
