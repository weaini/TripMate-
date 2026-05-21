package com.huashidai.lvyouapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notification_preferences")
@Data
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Boolean enableSystem = true;
    private Boolean enableActivity = true;
    private Boolean enableComment = true;
    private Boolean enableReview = true; // 审核结果
}

