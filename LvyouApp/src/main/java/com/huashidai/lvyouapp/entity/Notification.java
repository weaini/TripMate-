package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Entity
@Table(name = "notifications")
@Data
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;
    
    @Column(length = 500)
    private String relatedUrl;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        SYSTEM,         // 系统通知
        ACTIVITY,       // 活动相关
        COMMENT,        // 评论相关
        LIKE,           // 点赞相关
        FOLLOW,         // 关注相关
        MESSAGE         // 私信相关
    }
    
    public enum NotificationStatus {
        UNREAD,         // 未读
        READ,           // 已读
        DELETED         // 已删除
    }
}
