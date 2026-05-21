package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点实体类
 */
@Entity
@Table(name = "attractions")
@Data
public class Attraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 1000)
    private String introduction; // 详细介绍
    
    @Column(length = 50)
    private String country;
    
    @Column(length = 50)
    private String province; // 省份
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 200)
    private String address; // 详细地址
    
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude; // 纬度
    
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude; // 经度
    
    @Column(length = 1000)
    private String coverImage; // 封面图片
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttractionType type; // 景点类型
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttractionLevel level; // 景点等级（5A、4A等）
    
    @Column(precision = 10, scale = 2)
    private BigDecimal ticketPrice; // 门票价格
    
    @Column(length = 200)
    private String openingHours; // 开放时间
    
    @Column(length = 200)
    private String contactPhone; // 联系电话
    
    @Column(length = 500)
    private String website; // 官方网站
    
    @Column(length = 1000)
    private String tips; // 游玩提示
    
    private Integer viewCount = 0; // 浏览量
    
    private Integer favoriteCount = 0; // 收藏数
    
    private Integer rating = 0; // 评分（0-100）
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttractionStatus status = AttractionStatus.ACTIVE; // 状态
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // 创建者（管理员）
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 景点图片
    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttractionImage> images;
    
    // 景点收藏
    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttractionFavorite> favorites;
    
    /**
     * 景点类型枚举
     */
    public enum AttractionType {
        NATURAL,        // 自然景观
        CULTURAL,       // 文化古迹
        ENTERTAINMENT,  // 娱乐休闲
        MUSEUM,         // 博物馆
        PARK,           // 公园
        TEMPLE,         // 寺庙
        MOUNTAIN,       // 山岳
        WATER,          // 水域
        ARCHITECTURE,   // 建筑
        OTHER           // 其他
    }
    
    /**
     * 景点等级枚举
     */
    public enum AttractionLevel {
        LEVEL_5A,       // 5A级
        LEVEL_4A,       // 4A级
        LEVEL_3A,       // 3A级
        LEVEL_2A,       // 2A级
        LEVEL_1A,       // 1A级
        UNRATED         // 未评级
    }
    
    /**
     * 景点状态枚举
     */
    public enum AttractionStatus {
        ACTIVE,         // 正常
        INACTIVE,       // 停用
        DELETED         // 已删除
    }
}

