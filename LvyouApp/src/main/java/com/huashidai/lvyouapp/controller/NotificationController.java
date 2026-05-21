package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.entity.Notification;
import com.huashidai.lvyouapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.listMyNotifications(PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        long count = notificationService.getMyUnreadCount();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("count", count);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Map<String, Object>> markRead(@RequestBody List<Long> ids) {
        int updated = notificationService.markRead(ids);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("updated", updated);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> readAll() {
        int updated = notificationService.markAllRead();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("updated", updated);
        return ResponseEntity.ok(resp);
    }

    // 管理端或系统内部创建通知
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','EXPERT')")
    public ResponseEntity<Notification> create(@RequestParam Long userId,
                                               @RequestParam String title,
                                               @RequestParam String content,
                                               @RequestParam(defaultValue = "SYSTEM") Notification.NotificationType type,
                                               @RequestParam(required = false) String relatedUrl) {
        return ResponseEntity.ok(notificationService.createNotification(userId, title, content, type, relatedUrl));
    }
}

