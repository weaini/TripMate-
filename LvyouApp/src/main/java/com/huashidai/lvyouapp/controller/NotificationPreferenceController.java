package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.entity.NotificationPreference;
import com.huashidai.lvyouapp.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/me")
    public ResponseEntity<NotificationPreference> me() {
        return ResponseEntity.ok(preferenceService.getMyPreference());
    }

    @PutMapping("/me")
    public ResponseEntity<NotificationPreference> update(@RequestBody NotificationPreference preference) {
        return ResponseEntity.ok(preferenceService.updateMyPreference(preference));
    }
}

