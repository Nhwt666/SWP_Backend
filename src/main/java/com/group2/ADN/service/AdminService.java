package com.group2.ADN.service;

import com.group2.ADN.repository.TopUpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.entity.Ticket;
import com.group2.ADN.repository.UserRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final TopUpHistoryRepository topUpHistoryRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getDepositStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> statsByMethod = topUpHistoryRepository.getDepositStatsByPaymentMethod();
        BigDecimal totalDeposits = topUpHistoryRepository.getTotalSuccessfulDeposit();

        BigDecimal momoTotal = BigDecimal.ZERO;
        BigDecimal paypalTotal = BigDecimal.ZERO;

        for (Map<String, Object> stat : statsByMethod) {
            String method = (String) stat.get("paymentMethod");
            BigDecimal amount = (BigDecimal) stat.get("totalAmount");
            if ("MOMO".equalsIgnoreCase(method)) {
                momoTotal = amount;
            } else if ("PAYPAL".equalsIgnoreCase(method)) {
                paypalTotal = amount;
            }
        }

        stats.put("totalDeposits", totalDeposits != null ? totalDeposits : BigDecimal.ZERO);
        stats.put("momoTotal", momoTotal);
        stats.put("paypalTotal", paypalTotal);

        return stats;
    }

    public Map<String, Object> getSpendingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalSpent = ticketRepository.sumTotalAmount();
        stats.put("totalTicketSpending", totalSpent != null ? totalSpent : BigDecimal.ZERO);
        return stats;
    }

    /**
     * Admin rejects a ticket if it is of type OTHER and status is PENDING.
     * @param ticket The ticket to reject
     * @param rejectedReason The reason for rejection
     * @param status The status to set (should be REJECTED)
     * @return The updated ticket
     * @throws RuntimeException if the ticket is not eligible for rejection
     */
    public Ticket adminRejectTicket(Ticket ticket, String rejectedReason, String status) {
        if (ticket.getType() != com.group2.ADN.entity.TicketType.OTHER) {
            throw new RuntimeException("Only tickets of type OTHER can be rejected by admin.");
        }
        if (ticket.getStatus() != com.group2.ADN.entity.TicketStatus.PENDING) {
            throw new RuntimeException("Only PENDING tickets can be rejected.");
        }
        // Nếu status truyền lên hợp lệ thì set, không thì mặc định REJECTED
        com.group2.ADN.entity.TicketStatus newStatus;
        try {
            newStatus = (status != null) ? com.group2.ADN.entity.TicketStatus.valueOf(status) : com.group2.ADN.entity.TicketStatus.REJECTED;
        } catch (Exception e) {
            newStatus = com.group2.ADN.entity.TicketStatus.REJECTED;
        }
        ticket.setStatus(newStatus);
        ticket.setRejectedReason(rejectedReason);
        // Refund amount to user wallet if applicable
        if (ticket.getAmount() != null && ticket.getCustomer() != null) {
            java.math.BigDecimal currentBalance = ticket.getCustomer().getWalletBalance();
            if (currentBalance == null) currentBalance = java.math.BigDecimal.ZERO;
            ticket.getCustomer().setWalletBalance(currentBalance.add(ticket.getAmount()));
            userRepository.save(ticket.getCustomer());
        }
        return ticketRepository.save(ticket);
    }

    public List<Map<String, Object>> getRecentCompletedTickets() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime sevenDaysAgo = now.minusDays(7);
        List<Ticket> tickets = ticketRepository.findByStatusAndCompletedAtBetween(
            com.group2.ADN.entity.TicketStatus.COMPLETED, sevenDaysAgo, now);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Ticket t : tickets) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", t.getId());
            map.put("customerName", t.getCustomer() != null ? t.getCustomer().getFullName() : null);
            map.put("status", t.getStatus());
            map.put("completedAt", t.getCompletedAt());
            result.add(map);
        }
        return result;
    }
} 