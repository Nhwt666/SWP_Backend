package com.group2.ADN.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@Entity
@Table(name = "topup_history")
public class TopUpHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private LocalDateTime createdAt;

    private String paymentId;

    private String payerId;
}
