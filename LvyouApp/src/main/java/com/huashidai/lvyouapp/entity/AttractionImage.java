package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 景点图片实体类
 */
@Entity
@Table(name = "attraction_images")
@Data
public class AttractionImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;
    
    @Column(nullable = false, length = 500)
    private String imageUrl;
    
    @Column(length = 200)
    private String caption; // 图片说明
    
    private Integer sortOrder = 0; // 排序
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

