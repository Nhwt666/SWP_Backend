package com.group2.ADN.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "price")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType type;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    public Price() {
        // Constructor for Price entity
    }

    public Price(BigDecimal value, String currency, String name, PriceType type) {
        this.value = value;
        this.currency = currency;
        this.name = name;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PriceType getType() { return type; }
    public void setType(PriceType type) { this.type = type; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
} 