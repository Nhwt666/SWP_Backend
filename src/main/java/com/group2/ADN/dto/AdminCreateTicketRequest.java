package com.group2.ADN.dto;

import com.group2.ADN.entity.TestMethod;
import com.group2.ADN.entity.TicketStatus;
import com.group2.ADN.entity.TicketType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminCreateTicketRequest {
    private Long customerId;
    private TicketType type;
    private TestMethod method;
    private TicketStatus status = TicketStatus.PENDING; // Default status
    private BigDecimal amount;
    private String email;
    private String phone;
    private String address;
    private String sample1Name;
    private String sample2Name;
    private String reason;
} 