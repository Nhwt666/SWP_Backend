package com.group2.ADN.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double percentage;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;
} 