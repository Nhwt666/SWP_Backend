package com.group2.ADN.dto;

import com.group2.ADN.entity.PriceType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminUpdatePriceRequest {
    private BigDecimal value;
    private String currency;
    private String name;
    private PriceType type;
} 