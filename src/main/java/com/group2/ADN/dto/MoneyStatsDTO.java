package com.group2.ADN.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyStatsDTO {
    private BigDecimal totalTopUp;
    private BigDecimal totalPaidForTickets;
} 