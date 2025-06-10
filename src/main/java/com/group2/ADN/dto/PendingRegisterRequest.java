package com.group2.ADN.dto;

import com.group2.ADN.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PendingRegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 25, message = "Họ tên phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Za-zÀ-Ỹà-ỹ\\s]+$", message = "Họ tên chỉ được chứa chữ cái và dấu cách")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ (ví dụ: 0912345678)")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 32, message = "Mật khẩu phải từ 8 đến 32 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
            message = "Mật khẩu phải gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
}
