package com.group2.ADN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String fullName;

    private String email;
    private String phone;
    private String passwordHash;
    private String otp;
    private LocalDateTime expiresAt;
    private boolean verified;
}
