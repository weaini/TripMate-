package com.huashidai.lvyouapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态/帖子实体类
 */
@Entity
@Table(name = "posts")
@Data
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(length = 200)
    private String location;
    
    @Column(length = 20)
    private String latitude;
    
    @Column(length = 20)
    private String longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type = PostType.DYNAMIC;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PENDING;
    
    private Integer likeCount = 0;
    
    private Integer commentCount = 0;
    
    private Integer shareCount = 0;
    
    private Integer viewCount = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 动态图片
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostImage> images;
    
    // 动态评论
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    // 动态点赞
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostLike> likes;
    
    public enum PostType {
        DYNAMIC,        // 实时动态
        STRATEGY,       // 攻略分享
        ACTIVITY        // 活动相关
    }
    
    public enum PostStatus {
        PENDING,        // 待审核
        APPROVED,       // 已通过
        REJECTED,       // 已拒绝
        DELETED         // 已删除
    }
}
