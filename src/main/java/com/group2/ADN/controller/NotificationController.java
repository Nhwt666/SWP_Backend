package com.group2.ADN.controller;

import com.group2.ADN.entity.User;
import com.group2.ADN.entity.Notification;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        // Map về response mong muốn
        List<Map<String, Object>> result = notifications.stream()
            .map(n -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", n.getId());
                map.put("message", n.getMessage());
                map.put("time", n.getTime());
                map.put("read", n.isRead());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("success", true));
    }
} 