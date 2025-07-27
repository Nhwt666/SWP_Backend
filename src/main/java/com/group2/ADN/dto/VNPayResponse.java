package com.group2.ADN.dto;

import lombok.Data;

@Data
public class VNPayResponse {
    private String paymentUrl;
    private String code;
    private String message;
    private String vnp_TxnRef;  // Transaction reference
} 