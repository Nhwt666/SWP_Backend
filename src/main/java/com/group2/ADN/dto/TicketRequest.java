package com.group2.ADN.dto;

import lombok.Getter;
import lombok.Setter;
import com.group2.ADN.entity.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter
@Setter
public class TicketRequest {
    private String type;
    private String method;
    private String reason;
    private Long customerId;
    private TicketStatus status;

    private String address;
    private String phone;
    private String email;

    private BigDecimal amount;
    private String result;

    private String personAName;
    private String personBName;

    private LocalDate appointmentDate;

    private String sample1Name;
    private String sample2Name;

    private String voucherCode;
}