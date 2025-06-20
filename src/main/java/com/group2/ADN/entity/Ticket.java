package com.group2.ADN.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(length = 500)
    private String reason;


    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("user-tickets")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    @JsonBackReference("staff-tickets")
    private User staff;  // Nullable until assigned

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @ManyToOne
    @JoinColumn(name = "result_id")
    private Result result;

    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;
}
