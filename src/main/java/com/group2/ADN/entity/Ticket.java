package com.group2.ADN.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
    private User customer;

    @ManyToOne
    @JoinColumn(name = "staff_id")
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

    @Column(length = 500)
    private String resultString;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @OneToOne
    @JoinColumn(name = "result_id")
    @JsonManagedReference
    private Result result;

    @Column(length = 255, columnDefinition = "nvarchar")
    private String sampleFromPersonA;

    @Column(length = 255, columnDefinition = "nvarchar")
    private String sampleFromPersonB;

    private LocalDate appointmentDate;

    @Column(name = "sample1_name")
    private String sample1Name;
    
    @Column(name = "sample2_name") 
    private String sample2Name;
}
