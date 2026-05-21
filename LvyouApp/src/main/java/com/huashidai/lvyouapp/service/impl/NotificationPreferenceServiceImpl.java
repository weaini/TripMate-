package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.entity.NotificationPreference;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.NotificationPreferenceRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.NotificationPreferenceService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationPreference getMyPreference() {
        Long uid = SecurityUtils.getCurrentUserId();
        if (uid == null) { throw new RuntimeException("未登录"); }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        return preferenceRepository.findByUser(user).orElseGet(() -> {
            NotificationPreference p = new NotificationPreference();
            p.setUser(user);
            return preferenceRepository.save(p);
        });
    }

    @Override
    @Transactional
    public NotificationPreference updateMyPreference(NotificationPreference pref) {
        NotificationPreference current = getMyPreference();
        current.setEnableSystem(Boolean.TRUE.equals(pref.getEnableSystem()));
        current.setEnableActivity(Boolean.TRUE.equals(pref.getEnableActivity()));
        current.setEnableComment(Boolean.TRUE.equals(pref.getEnableComment()));
        current.setEnableReview(Boolean.TRUE.equals(pref.getEnableReview()));
        return preferenceRepository.save(current);
    }
}

