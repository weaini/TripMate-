package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 积分记录实体类
 */
@Entity
@Table(name = "points_records")
@Data
public class PointsRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer points; // 积分变化（正数为获得，负数为消费）
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointsType type;
    
    @Column(length = 200)
    private String description;
    
    @Column(length = 200)
    private String relatedType; // 关联类型：POST, ACTIVITY, STRATEGY等
    
    private Long relatedId; // 关联ID
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum PointsType {
        EARN,           // 获得积分
        SPEND,          // 消费积分
        REFUND,         // 退还积分
        ADMIN_ADJUST    // 管理员调整
    }
}
