package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.entity.NotificationPreference;

public interface NotificationPreferenceService {
    NotificationPreference getMyPreference();
    NotificationPreference updateMyPreference(NotificationPreference pref);
}

