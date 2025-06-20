package com.group2.ADN.repository;

import com.group2.ADN.entity.TopUpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TopUpHistoryRepository extends JpaRepository<TopUpHistory, Long> {
    List<TopUpHistory> findByUserId(Long userId);
    Optional<TopUpHistory> findByPaymentId(String paymentId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TopUpHistory t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    BigDecimal getTotalTopUpInPeriod(LocalDateTime startDate, LocalDateTime endDate);
}