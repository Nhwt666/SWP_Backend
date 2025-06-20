package com.group2.ADN.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorWithTicketCountDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private int ticketCount;
} 