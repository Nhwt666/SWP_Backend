package com.group2.ADN.dto;

import com.group2.ADN.entity.TicketStatus;
import lombok.Data;

@Data
public class AdminUpdateTicketRequest {
    private TicketStatus status;
    private Long staffId;
    private String resultString;
} 