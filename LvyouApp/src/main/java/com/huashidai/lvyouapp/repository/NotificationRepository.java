package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Notification;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndStatus(User user, Notification.NotificationStatus status);

    @Modifying
    @Query("update Notification n set n.status = 'READ' where n.user = :user and n.id in :ids")
    int markReadByIds(@Param("user") User user, @Param("ids") java.util.List<Long> ids);

    @Modifying
    @Query("update Notification n set n.status = 'READ' where n.user = :user and n.status = 'UNREAD'")
    int markAllRead(@Param("user") User user);
}

