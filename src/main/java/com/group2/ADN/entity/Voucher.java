package com.group2.ADN.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // 'percent' hoặc 'amount'

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "used_count")
    private Integer usedCount;

    @Column
    private String status;

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 