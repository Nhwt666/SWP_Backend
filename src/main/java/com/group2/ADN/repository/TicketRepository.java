package com.group2.ADN.repository;

import com.group2.ADN.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomer(User Customer);

    List<Ticket> findByStaff(User Staff);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByTypeAndMethod(TicketType type, TestMethod method);

    int countByStaffAndStatusIn(User staff, List<TicketStatus> statuses);

    int countByStaffAndStatus(User staff, TicketStatus status);

    List<Ticket> findByStatusAndStaffIsNull(TicketStatus status);

    @Query("SELECT SUM(t.amount) FROM Ticket t")
    BigDecimal sumTotalAmount();

    @Query("SELECT CAST(t.createdAt AS date) as date, COUNT(t.id) as count FROM Ticket t WHERE t.createdAt >= :from AND t.createdAt <= :to GROUP BY CAST(t.createdAt AS date) ORDER BY date ASC")
    List<Object[]> countTicketsByCreatedAtBetweenGroupByDate(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    @Query("SELECT t.status, COUNT(t.id) FROM Ticket t WHERE (:from IS NULL OR t.createdAt >= :from) AND (:to IS NULL OR t.createdAt <= :to) GROUP BY t.status")
    List<Object[]> countTicketsByStatusWithFilter(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    List<Ticket> findByStatusAndCompletedAtBetween(TicketStatus status, java.time.LocalDateTime from, java.time.LocalDateTime to);

    @Query("SELECT t FROM Ticket t WHERE t.rating IS NOT NULL OR t.feedback IS NOT NULL ORDER BY t.feedbackDate DESC")
    List<Ticket> findByRatingIsNotNullOrFeedbackIsNotNull();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Ticket t WHERE t.customer.id = :userId AND t.status != 'CANCELED'")
    BigDecimal sumTotalPriceByUserId(@Param("userId") Long userId);
}