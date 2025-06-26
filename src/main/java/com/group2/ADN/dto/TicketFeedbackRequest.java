package com.group2.ADN.dto;
import lombok.Data;

@Data
public class TicketFeedbackRequest {
    private String feedback;
    private Integer rating; // 1 to 5
} 