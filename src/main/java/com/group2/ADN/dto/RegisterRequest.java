package com.group2.ADN.dto;



import com.group2.ADN.entity.UserRole;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
}