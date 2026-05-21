package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.entity.Notification;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.NotificationRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.NotificationService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String content, Notification.NotificationType type, String relatedUrl) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRelatedUrl(relatedUrl);
        Notification saved = notificationRepository.save(n);

        // 推送到用户专属topic
        try {
            String destination = "/topic/user/" + user.getId() + "/notifications";
            messagingTemplate.convertAndSend(destination, saved);
        } catch (Exception e) {
            log.warn("通知推送失败: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public Page<Notification> listMyNotifications(Pageable pageable) {
        Long uid = SecurityUtils.getCurrentUserId();
        if (uid == null) {
            throw new RuntimeException("未登录");
        }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public long getMyUnreadCount() {
        Long uid = SecurityUtils.getCurrentUserId();
        if (uid == null) {
            throw new RuntimeException("未登录");
        }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        return notificationRepository.countByUserAndStatus(user, Notification.NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public int markRead(java.util.List<Long> ids) {
        Long uid = SecurityUtils.getCurrentUserId();
        if (uid == null) {
            throw new RuntimeException("未登录");
        }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        return notificationRepository.markReadByIds(user, ids);
    }

    @Override
    @Transactional
    public int markAllRead() {
        Long uid = SecurityUtils.getCurrentUserId();
        if (uid == null) {
            throw new RuntimeException("未登录");
        }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        return notificationRepository.markAllRead(user);
    }
}

