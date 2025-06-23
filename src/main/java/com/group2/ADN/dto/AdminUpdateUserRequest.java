package com.group2.ADN.dto;

import com.group2.ADN.entity.UserRole;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminUpdateUserRequest {
    private String fullName;
    private String phone;
    private String address;
    private UserRole role;
    private BigDecimal walletBalance;
} 