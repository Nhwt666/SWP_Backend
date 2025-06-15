package com.group2.ADN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequest {
    private String type;
    private String method;
    private String reason;
    private Long customerId;

    private String address;
    private String phone;
    private String email;
}