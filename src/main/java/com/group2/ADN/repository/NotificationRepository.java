package com.group2.ADN.repository;

import com.group2.ADN.entity.Notification;
import com.group2.ADN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByTimeDesc(User user);
} 