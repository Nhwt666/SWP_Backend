package com.group2.ADN.service;

import com.group2.ADN.repository.TopUpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.group2.ADN.repository.TicketRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final TopUpHistoryRepository topUpHistoryRepository;
    private final TicketRepository ticketRepository;

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
} 