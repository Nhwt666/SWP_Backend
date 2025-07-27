package com.group2.ADN.dto;

import lombok.Data;

@Data
public class VNPayRequest {
    private String vnp_Version;
    private String vnp_Command;
    private String vnp_TmnCode;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_CreateDate;
    private String vnp_CurrCode;
    private String vnp_IpAddr;
    private String vnp_Locale;
    private String vnp_OrderInfo;
    private String vnp_OrderType;
    private String vnp_ReturnUrl;
    private String vnp_TxnRef;
    private String vnp_ExpireDate;
} 