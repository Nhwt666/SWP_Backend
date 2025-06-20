package com.group2.ADN.service;

import com.group2.ADN.dto.MoneyStatsDTO;
import com.group2.ADN.repository.TicketRepository;
import com.group2.ADN.repository.TopUpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final TopUpHistoryRepository topUpHistoryRepository;
    private final TicketRepository ticketRepository;

    public MoneyStatsDTO getMoneyStats(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalTopUp = topUpHistoryRepository.getTotalTopUpInPeriod(startDate, endDate);
        BigDecimal totalPaid = ticketRepository.getTotalPaidForTicketsInPeriod(startDate, endDate);
        
        if (totalTopUp == null) totalTopUp = BigDecimal.ZERO;
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        return new MoneyStatsDTO(totalTopUp, totalPaid);
    }
} 