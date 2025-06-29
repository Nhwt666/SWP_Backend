package com.group2.ADN.entity;

public enum TicketStatus {
    PENDING,
    IN_PROGRESS,
    RECEIVED,    // Member xác nhận đã nhận kit
    CONFIRMED,   // Trạng thái ban đầu cho CIVIL SELF_TEST
    COMPLETED,
    CANCELLED,
    REJECTED,
}
