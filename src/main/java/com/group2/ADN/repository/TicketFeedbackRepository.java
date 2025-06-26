package com.group2.ADN.repository;

import com.group2.ADN.entity.TicketFeedback;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long> {
    Optional<TicketFeedback> findByTicketAndUser(Ticket ticket, User user);
} 