package com.group2.ADN.service;

import com.group2.ADN.entity.Notification;
import com.group2.ADN.entity.User;
import com.group2.ADN.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByTimeDesc(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByTimeDesc(user);
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .time(LocalDateTime.now())
                .read(false)
                .build();
        notificationRepository.save(notification);
    }
} 