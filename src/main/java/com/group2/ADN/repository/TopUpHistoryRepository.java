package com.group2.ADN.repository;

import com.group2.ADN.entity.TopUpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TopUpHistoryRepository extends JpaRepository<TopUpHistory, Long> {
    List<TopUpHistory> findByUserId(Long userId);
    Optional<TopUpHistory> findByPaymentId(String paymentId);

    @Query("SELECT t.paymentMethod as paymentMethod, SUM(t.amount) as totalAmount " +
            "FROM TopUpHistory t " +
            "WHERE t.status = 'SUCCESS' " +
            "GROUP BY t.paymentMethod")
    List<Map<String, Object>> getDepositStatsByPaymentMethod();

    @Query("SELECT SUM(t.amount) " +
            "FROM TopUpHistory t " +
            "WHERE t.status = 'SUCCESS'")
    BigDecimal getTotalSuccessfulDeposit();
}