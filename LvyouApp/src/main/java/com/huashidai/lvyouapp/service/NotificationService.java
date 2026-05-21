package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Notification createNotification(Long userId, String title, String content,
                                    Notification.NotificationType type, String relatedUrl);

    Page<Notification> listMyNotifications(Pageable pageable);

    long getMyUnreadCount();

    int markRead(java.util.List<Long> ids);

    int markAllRead();
}

