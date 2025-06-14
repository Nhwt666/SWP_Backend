package com.group2.ADN.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String otp;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String newPassword;
}
