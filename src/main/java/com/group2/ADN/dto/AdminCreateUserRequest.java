package com.group2.ADN.dto;

import com.group2.ADN.entity.UserRole;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String email;
    private String password;
    private String fullName;
    private UserRole role;
} 