package com.huashidai.lvyouapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"posts", "activityParticipants", "organizedActivities", "strategies"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 200)
    private String avatar;
    
    @Column(length = 10)
    private String gender;
    
    private Integer age;
    
    @Column(length = 200)
    private String location;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 20)
    private String birthday;
    
    @Column(length = 200)
    private String interests;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.TOURIST;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    private Integer points = 0;
    
    private Integer followersCount = 0;
    
    private Integer followingCount = 0;
    
    private Boolean isVerified = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 用户发布的动态（避免 Jackson 序列化循环引用）
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Post> posts;
    
    // 用户参与的活动
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ActivityParticipant> activityParticipants;
    
    // 用户组织的活动
    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Activity> organizedActivities;
    
    // 用户创建的攻略
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Strategy> strategies;
    
    public enum UserRole {
        TOURIST,        // 游客
        EXPERT,         // 旅游达人
        ADMIN           // 管理员
    }
    
    public enum UserStatus {
        ACTIVE,         // 活跃
        INACTIVE,       // 非活跃
        BANNED          // 封禁
    }
}
