package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.NotificationPreference;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUser(User user);
}

