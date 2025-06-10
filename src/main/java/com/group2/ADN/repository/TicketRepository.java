package com.group2.ADN.repository;

import com.group2.ADN.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerId(User Customer);

    List<Ticket> findByStaff(User Staff);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByTypeandMethod(TicketType type, TestMethod method);
}