package com.group2.ADN.controller;

import com.group2.ADN.entity.User;
import com.group2.ADN.entity.Notification;
import com.group2.ADN.repository.UserRepository;
import com.group2.ADN.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        
        List<Map<String, Object>> result = notifications.stream()
            .map(n -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", n.getId());
                map.put("message", n.getMessage());
                map.put("type", n.getType().name());
                map.put("ticketId", n.getTicketId());
                map.put("isRead", n.getIsRead());
                map.put("createdAt", n.getCreatedAt());
                map.put("expiresAt", n.getExpiresAt());
                map.put("statusChange", n.getStatusChange());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/ticket/{ticketId}/delete-old")
    public ResponseEntity<?> deleteOldNotifications(@PathVariable Long ticketId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        // Chỉ admin/staff mới có thể xóa notification
        if (user.getRole().name().equals("CUSTOMER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Insufficient permissions"));
        }
        
        notificationService.deleteOldNotifications(ticketId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/cleanup-expired")
    public ResponseEntity<?> cleanupExpiredNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        // Chỉ admin/staff mới có thể cleanup
        if (user.getRole().name().equals("CUSTOMER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Insufficient permissions"));
        }
        
        notificationService.cleanupExpiredNotifications();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Cron job chạy mỗi giờ để xóa notification hết hạn
    @Scheduled(fixedRate = 3600000) // 1 giờ
    public void cleanupExpiredNotificationsScheduled() {
        log.info("🕐 Bắt đầu cleanup notification hết hạn...");
        notificationService.cleanupExpiredNotifications();
    }
} 