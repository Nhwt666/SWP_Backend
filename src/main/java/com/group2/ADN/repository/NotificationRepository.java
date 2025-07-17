package com.group2.ADN.repository;

import com.group2.ADN.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy notification theo user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Lấy notification chưa đọc theo user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Lấy notification theo ticket
    List<Notification> findByTicketIdAndIsReadFalse(Long ticketId);
    
    // Lấy notification chưa đọc theo ticket và cũ hơn 1 ngày
    List<Notification> findByTicketIdAndIsReadFalseAndCreatedAtBefore(Long ticketId, java.time.LocalDateTime cutoff);
    
    // Lấy notification hết hạn
    List<Notification> findByExpiresAtBefore(LocalDateTime dateTime);
    
    // Đếm notification chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);
} 