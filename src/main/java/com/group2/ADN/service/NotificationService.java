package com.group2.ADN.service;

import com.group2.ADN.entity.Notification;
import com.group2.ADN.entity.NotificationType;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.NotificationRepository;
import com.group2.ADN.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Notification n : notifications) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    public void createNotification(User user, String message, Long ticketId, String type) {
        NotificationType notificationType = NotificationType.INFO;
        if ("WARNING".equals(type)) {
            notificationType = NotificationType.WARNING;
        } else if ("ERROR".equals(type)) {
            notificationType = NotificationType.ERROR;
        }

        Notification notification = Notification.builder()
                .userId(user.getId())
                .message(message)
                .type(notificationType)
                .ticketId(ticketId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(3)) // 3 ngày
                .build();
        notificationRepository.save(notification);
    }

    public void createNotification(User user, String message) {
        createNotification(user, message, null, "INFO");
    }

    public void createStatusChangeNotification(User user, Long ticketId, String oldStatus, String newStatus) {
        String message = String.format(
            "Ticket #%d đã chuyển từ '%s' sang '%s'",
            ticketId,
            getStatusDisplayName(oldStatus),
            getStatusDisplayName(newStatus)
        );

        // Xóa notification cũ trước khi tạo mới
        deleteOldNotifications(ticketId);

        // Tạo notification mới
        Notification notification = Notification.builder()
                .userId(user.getId())
                .message(message)
                .type(NotificationType.INFO)
                .ticketId(ticketId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(3)) // 3 ngày
                .statusChange(String.format("{\"from\": \"%s\", \"to\": \"%s\"}", oldStatus, newStatus))
                .build();

        notificationRepository.save(notification);
        
        log.info("✅ Tạo notification: Ticket #{} {} → {}", ticketId, oldStatus, newStatus);
    }

    @Transactional
    public void deleteOldNotifications(Long ticketId) {
        List<Notification> oldNotifications = notificationRepository.findByTicketIdAndIsReadFalse(ticketId);
        
        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("🗑️ Xóa {} notification cũ cho ticket #{}", oldNotifications.size(), ticketId);
        }
    }

    @Transactional
    public void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> expiredNotifications = notificationRepository.findByExpiresAtBefore(now);
        
        if (!expiredNotifications.isEmpty()) {
            notificationRepository.deleteAll(expiredNotifications);
            log.info("🧹 Xóa {} notification hết hạn", expiredNotifications.size());
        }
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to mark this notification as read");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private String getStatusDisplayName(String status) {
        switch (status) {
            case "PENDING": return "Chờ xử lý";
            case "IN_PROGRESS": return "Đang xử lý";
            case "RECEIVED": return "Đã nhận kit";
            case "CONFIRMED": return "Đã xác nhận Yêu Cầu";
            case "COMPLETED": return "Hoàn thành";
            case "REJECTED": return "Đã từ chối";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
} 