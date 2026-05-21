package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 动态图片实体类
 */
@Entity
@Table(name = "post_images")
@Data
public class PostImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(nullable = false, length = 500)
    private String imageUrl;
    
    @Column(length = 200)
    private String caption;
    
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
