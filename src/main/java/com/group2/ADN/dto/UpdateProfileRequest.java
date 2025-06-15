package com.group2.ADN.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 25, message = "Họ tên phải từ 2 đến 25 ký tự")
    @Pattern(regexp = "^[A-Za-zÀ-Ỵà-ỹ\\s]+$", message = "Họ tên chỉ được chứa chữ cái và dấu cách")
    private String fullName;

    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ (ví dụ: 0912345678)")
    private String phone;

    @Size(min = 5, max = 100, message = "Địa chỉ phải từ 5 đến 100 ký tự")
    @Pattern(regexp = "^[A-Za-zÀ-Ỵà-ỹ\\s]+$", message = "Địa chỉ được chứa chữ cái và dấu cách")
    private String address;
}
