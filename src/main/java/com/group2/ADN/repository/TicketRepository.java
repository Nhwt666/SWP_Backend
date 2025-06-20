package com.group2.ADN.repository;

import com.group2.ADN.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomer(User customer);

    List<Ticket> findByStaff(User staff);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByTypeAndMethod(TicketType type, TestMethod method);

    int countByStaffAndStatusIn(User staff, List<TicketStatus> statuses);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Ticket t")
    BigDecimal getTotalPaidForTickets();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Ticket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    BigDecimal getTotalPaidForTicketsInPeriod(LocalDateTime startDate, LocalDateTime endDate);

    List<Ticket> findByCustomerId(Long customerId);
}