package com.group2.ADN.repository;

import com.group2.ADN.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomer(User Customer);

    List<Ticket> findByStaff(User Staff);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByTypeAndMethod(TicketType type, TestMethod method);

    int countByStaffAndStatusIn(User staff, List<TicketStatus> statuses);

    List<Ticket> findByStatusAndStaffIsNull(TicketStatus status);

    @Query("SELECT SUM(t.amount) FROM Ticket t")
    BigDecimal sumTotalAmount();
}