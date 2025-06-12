package com.group2.ADN.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {
    private Long userId;
    private BigDecimal amount;
}
